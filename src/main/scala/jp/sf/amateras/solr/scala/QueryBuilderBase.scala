package jp.sf.amateras.solr.scala

import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.util.NamedList
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import jp.sf.amateras.solr.scala.query._
import scala.collection.JavaConverters._

abstract class QueryBuilderBase(query: String)(implicit parser: ExpressionParser) {

  protected val solrQuery = new SolrQuery()
  
  protected var id: String = "id"
  protected var highlightField: String = null
  protected var recommendFlag: Boolean = false

  /**
   * Sets the field name of the unique key.
   * 
   * @param id the field name of the unique key (default is "id").
   */
  def id(id: String): this.type = {
    this.id = id
    this
  }
    
  /**
   * Sets field names to retrieve by this query.
   *
   * @param fields field names
   */
  def fields(fields: String*): this.type = {
    fields.foreach { field =>
      solrQuery.addField(field)
    }
    this
  }

  /**
   * Sets the sorting field name and its order.
   *
   * @param field the sorting field name
   * @param order the sorting order
   */
  def sortBy[T <: QueryBuilderBase](field: String, order: Order): this.type = {
    solrQuery.setSort(field, order)
    this
  }

  /**
   * Sets grouping field names.
   *
   * @param fields field names
   */
  def groupBy[T <: QueryBuilderBase](fields: String*): this.type = {
    if(fields.size > 0){
      solrQuery.setParam("group", "true")
      solrQuery.setParam("group.field", fields: _*)
    }
    this
  }

  /**
   * Sets facet field names.
   *
   * @param fields field names
   */
  def facetFields[T <: QueryBuilderBase](fields: String*): this.type = {
    solrQuery.setFacet(true)
    solrQuery.addFacetField(fields: _*)
    this
  }

  /**
   * Specifies the maximum number of results to return.
   * 
   * @param rows number of results
   */
  def rows(rows: Int): this.type = {
    solrQuery.setRows(rows)
    this
  }
    
  /**
   * Sets the offset to start at in the result set.
   * 
   * @param start zero-based offset
   */
  def start(start: Int): this.type = {
    solrQuery.setStart(start)
    this
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
  def highlight(field: String, size: Int = 100, prefix: String = "", postfix: String = ""): this.type = {
    solrQuery.setHighlight(true)
    solrQuery.addHighlightField(field)
    solrQuery.setHighlightSnippets(1)
    solrQuery.setHighlightFragsize(size)
    if(prefix.nonEmpty){
      solrQuery.setHighlightSimplePre(prefix)
    }
    if(postfix.nonEmpty){
      solrQuery.setHighlightSimplePost(postfix)
    }
    highlightField = field
    this
  }
  
  /**
   * Configure to recommendation search.
   * If you call this method, the query returns documents similar to the query result instead of them.
   * 
   * @param fields field names of recommendation target 
   */
  def recommend(fields: String*): this.type = {
    solrQuery.set("mlt", true)
    solrQuery.set("mlt.fl", fields.mkString(","))
    solrQuery.set("mlt.mindf", 1)
    solrQuery.set("mlt.mintf", 1)
    solrQuery.set("mlt.count", 10)
    recommendFlag = true
    this
  }

  protected def responseToMap(response: QueryResponse): MapQueryResult = {
    val highlight = response.getHighlighting()

    def toList(docList: SolrDocumentList): List[Map[String, Any]] = {
      (for(i <- 0 to docList.size() - 1) yield {
        val doc = docList.get(i)
        val map = doc.getFieldNames().asScala.map { key => (key, doc.getFieldValue(key)) }.toMap
        if(solrQuery.getHighlight()){
          val id = doc.getFieldValue(this.id)
          if(id != null && highlight.get(id) != null && highlight.get(id).get(highlightField) != null){
            map + ("highlight" -> highlight.get(id).get(highlightField).get(0))
          } else {
            map + ("highlight" -> "")
          }
        } else {
          map
        }
      }).toList
    }

    val queryResult = if(recommendFlag){
      val mlt = response.getResponse.get("moreLikeThis").asInstanceOf[NamedList[Object]]
      val docs = mlt.getVal(0).asInstanceOf[java.util.List[SolrDocument]]
      docs.asScala.map { doc =>
        doc.getFieldNames.asScala.map { key => (key, doc.getFieldValue(key)) }.toMap
      }.toList
    } else { 
      solrQuery.getParams("group") match {
        case null => {
          toList(response.getResults())
        }
        case _ => {
          val groupResponse = response.getGroupResponse()
          groupResponse.getValues().asScala.map { groupCommand =>
            groupCommand.getValues().asScala.map { group =>
              toList(group.getResult())
            }.flatten
          }.flatten.toList
        }
      }
    }

    val facetResult = response.getFacetFields() match {
      case null => Map.empty[String, Map[String, Long]]
      case facetFields => facetFields.asScala.map { field => (
          field.getName(),
          field.getValues().asScala.map { value => (value.getName(), value.getCount()) }.toMap
      )}.toMap
    }

    MapQueryResult(response.getResults().getNumFound(), queryResult, facetResult)    
  }
  
  def responseToObject[T](response: QueryResponse)(implicit m: Manifest[T]): CaseClassQueryResult[T] = {
    val result = responseToMap(response)
    
    CaseClassQueryResult[T](
      result.numFound,
      result.documents.map { doc =>
        CaseClassMapper.map2class[T](doc)
      },
      result.facetFields
    )
  }

  }
