package com.github.takezoe.solr.scala

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.util.NamedList
import org.apache.solr.common.{SolrDocument, SolrDocumentList}

import scala.collection.JavaConverters._

trait QueryBuilderBase[Repr <: QueryBuilderBase[Repr]] {

  protected var solrQuery = new SolrQuery()

  protected var collection: String = null
  protected var id: String = "id"
  protected var highlightField: String = null
  protected var recommendFlag: Boolean = false

  protected def createCopy: Repr

  private def copy(newId: String = id,
                   newCollection: String = collection,
                   newHighlightField: String = highlightField,
                   newRecommendFlag: Boolean = recommendFlag): Repr = {
    val ret = createCopy
    ret.id = newId
    ret.collection = newCollection
    ret.highlightField = newHighlightField
    ret.recommendFlag = newRecommendFlag
    ret.solrQuery = solrQuery.getCopy
    ret
  }

  /**
    * Sets the field name of the unique key.
    *
    * @param id the field name of the unique key (default is "id").
    */
  def id(id: String): Repr = {
    copy(newId = id)
  }

  def collection(collection: String): Repr = {
    copy(newCollection = collection)
  }

  def facetQuery(fq: String): Repr = {
    val ret = copy()
    ret.solrQuery.addFacetQuery(fq)
    ret
  }

  def filteredQuery(fq: String*): Repr = {
    val ret = copy()
    ret.solrQuery.addFilterQuery(fq: _*)
    ret
  }

  /**
    * Sets the RequestHandler for the Solr query
    *
    * @param handler the name of the RequestHandler as defined in solrconfig.xml (default is "/select").
    */
  def setRequestHandler(handler: String): Repr = {
    val ret = copy()
    ret.solrQuery.setRequestHandler(handler)
    ret
  }

  /**
    * Sets field names to retrieve by this query.
    *
    * @param fields field names
    */
  def fields(fields: String*): Repr = {
    val ret = copy()
    fields.foreach { field =>
      ret.solrQuery.addField(field)
    }
    ret
  }

  /**
    * Sets the sorting field name and its order.
    *
    * @param field the sorting field name
    * @param order the sorting order
    */
  def sortBy(field: String, order: Order): Repr = {
    val ret = copy()
    ret.solrQuery.setSort(field, order)
    ret
  }

  /**
    * Sets grouping field names.
    *
    * @param fields field names
    */
  def groupBy(fields: String*): Repr = {
    val ret = copy()
    if (fields.size > 0) {
      ret.solrQuery.setParam("group", "true")
      ret.solrQuery.setParam("group.field", fields: _*)
    }
    ret
  }

  def enableGroupFacet(): Repr = {
    val ret = copy()
    ret.solrQuery.set("group.facet", true)
    ret
  }

  def enableGroupCount(): Repr = {
    val ret = copy()
    ret.solrQuery.set("group.ngroups", true)
    ret
  }

  def limitGrouping(limit: Int): Repr = {
    val ret = copy()
    ret.solrQuery.set("group.limit", limit)
    ret
  }

  def collapseBy(field: String, expandCount: Int): Repr = {
    val ret = copy()
    ret.solrQuery.addFilterQuery(s"{!collapse field=$field}")
    if (expandCount > 0) {
      ret.solrQuery.set("expand", "true")
      ret.solrQuery.set("expand.rows", expandCount)
    }
    ret
  }

  /**
    * Sets facet field names.
    *
    * @param fields field names
    */
  def facetFields(fields: String*): Repr = {
    val ret = copy()
    ret.solrQuery.setFacet(true)
    ret.solrQuery.addFacetField(fields: _*)
    ret
  }

  /**
    * Specifies the maximum number of results to return.
    *
    * @param rows number of results
    */
  def rows(rows: Int): Repr = {
    val ret = copy()
    ret.solrQuery.setRows(rows)
    ret
  }

  /**
    * Sets the offset to start at in the result set.
    *
    * @param start zero-based offset
    */
  def start(start: Int): Repr = {
    val ret = copy()
    ret.solrQuery.setStart(start)
    ret
  }

  /**
    * Configures to retrieve a highlighted snippet.
    * Highlighted snippet is set as the "highlight" property of the map or the case class.
    *
    * @param field the highlight field
    * @param size the highlight fragment size
    * @param prefix the prefix of highlighted ranges
    * @param postfix the postfix of highlighted ranges
    */
  def highlight(field: String,
                size: Int = 100,
                prefix: String = "",
                postfix: String = "",
                snippets: Int = 1): Repr = {
    val ret = copy(newHighlightField = field)
    ret.solrQuery.setHighlight(true)
    ret.solrQuery.addHighlightField(field)
    ret.solrQuery.setHighlightSnippets(snippets)
    ret.solrQuery.setHighlightFragsize(size)
    if (prefix.nonEmpty) {
      ret.solrQuery.setHighlightSimplePre(prefix)
    }
    if (postfix.nonEmpty) {
      ret.solrQuery.setHighlightSimplePost(postfix)
    }
    ret
  }

  /**
    * Configure to recommendation search.
    * If you call this method, the query returns documents similar to the query result instead of them.
    *
    * @param fields field names of recommendation target
    */
  def recommend(fields: String*): Repr = {
    val ret = copy(newRecommendFlag = true)
    ret.solrQuery.set("mlt", true)
    ret.solrQuery.set("mlt.fl", fields.mkString(","))
    ret.solrQuery.set("mlt.mindf", 1)
    ret.solrQuery.set("mlt.mintf", 1)
    ret.solrQuery.set("mlt.count", 10)
    ret
  }

  protected def docToMap(doc: SolrDocument) = {
    doc.getFieldNames.asScala.map { key =>
      key -> doc.getFieldValue(key)
    }.toMap
  }

  protected def responseToMap(response: QueryResponse): MapQueryResult = {
    val highlight = response.getHighlighting

    def toList(docList: SolrDocumentList): List[Map[String, Any]] = {
      (for (i <- 0 to docList.size - 1) yield {
        val doc = docList.get(i)
        val map = docToMap(doc)
        if (solrQuery.getHighlight) {
          val id = doc.getFieldValue(this.id)
          if (id != null && highlight.get(id) != null && highlight
                .get(id)
                .get(highlightField) != null) {
            map + ("highlights" -> highlight.get(id).get(highlightField))
          } else {
            throw new UnspecifiedIdError
          }
        } else {
          map
        }
      }).toList
    }

    val (numFound: Long,
         numGroupsFound: Long,
         queryResult: List[DocumentMap],
         groupResult: Map[String, List[Group]]) = if (recommendFlag) {
      val mlt =
        response.getResponse.get("moreLikeThis").asInstanceOf[NamedList[Object]]
      val docs = mlt.getVal(0).asInstanceOf[java.util.List[SolrDocument]]
      (response.getResults.getNumFound,
       docs.asScala.map(docToMap).toList,
       Map.empty)
    } else {
      solrQuery.getParams("group") match {
        case null => {
          (response.getResults.getNumFound,
           0L,
           toList(response.getResults),
           Map.empty)
        }
        case _ => {
          var matches = 0L
          var groupMatches = 0L
          val groupResult = response.getGroupResponse.getValues.asScala.map {
            groupCommand =>
              matches = matches + groupCommand.getMatches
              groupMatches = groupMatches + groupCommand.getNGroups
              groupCommand.getName -> groupCommand.getValues.asScala.map {
                group =>
                  Group(group.getGroupValue,
                        group.getResult.getNumFound,
                        toList(group.getResult))
              }.toList
          }.toMap

          (matches, groupMatches, List.empty, groupResult)
        }
      }
    }

    val facetResult = response.getFacetFields match {
      case null => Map.empty[String, Map[String, Long]]
      case facetFields =>
        facetFields.asScala.map { field =>
          (
            field.getName,
            field.getValues.asScala.map { value =>
              (value.getName, value.getCount)
            }.toMap
          )
        }.toMap

    }

    val facetPivotResult = response.getFacetPivot match {
      case null =>  Map.empty[String, List[FacetPivot]]
      case facetPivots => facetPivots.asScala.map { field => (
        field.getKey,
        field.getValue.asInstanceOf[List[FacetPivot]]
      )}.toMap
    }
    MapQueryResult(numFound,
                   numGroupsFound,
                   queryResult,
                   groupResult,
                   facetResult,
                   facetPivotResult,
                   response.getQTime
                  )
  }


  def responseToObject[T](response: QueryResponse)(
      implicit m: Manifest[T]): CaseClassQueryResult[T] = {
    val result = responseToMap(response)

    CaseClassQueryResult[T](
      result.numFound,
      result.documents.map { doc =>
        CaseClassMapper.map2class[T](doc)
      },
      result.facetFields
    )
  }

  /**
    * Added in response to #63 Missing spatial parameters while building solr query
    * * Sets Spatial field called pt.
    * @param lat
    * @param lng
    * @return
    */
  def spatialPt(lat: Double, lng: Double): Repr = {
    val ret = copy()
    val geoPoint = lat.toString + "," + lng.toString
    ret.solrQuery.set("pt", geoPoint)
    ret
  }

  /**
    * Added in response to #63 Missing spatial parameters while building solr query
    * Sets Spatial field named SField.
    * @param value : value for Sfield.
    */
  def spatialSfield(value: String): Repr = {
    val ret = copy()
    ret.solrQuery.set("sfield", value)
    ret
  }

  /**
    * Added in response to #63 Missing spatial parameters while building solr query
    * Enable Spatial is true.
    */
  def enableSpatial(): Repr = {
    val ret = copy()
    ret.solrQuery.set("spatial", true)
    ret
  }

  /**
    * Sets facet pivot field names.
    *
    * @param pivotField field names
    */
  def facetPivotFields(pivotField: String*): Repr = {
    val ret = copy()
    ret.solrQuery.addFacetPivotField(pivotField: _*)
    ret
  }

}
