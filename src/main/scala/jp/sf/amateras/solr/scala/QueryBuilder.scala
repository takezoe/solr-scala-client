package jp.sf.amateras.solr.scala

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrDocumentList

import jp.sf.amateras.solr.scala.query.ExpressionParser
import jp.sf.amateras.solr.scala.query.QueryTemplate
import org.apache.solr.client.solrj.response.QueryResponse

class QueryBuilder(server: SolrServer, query: String)(implicit parser: ExpressionParser) {

  private val solrQuery = new SolrQuery()
  private var id: String = "id"
  private var highlightField: String = null

  /**
   * Sets the field name of the unique key.
   * 
   * @param the field name of the unique key (default is "id").
   */
  def id(id: String): QueryBuilder = {
    this.id = id
    this
  }
    
  /**
   * Sets field names to retrieve by this query.
   *
   * @param fields field names
   */
  def fields(fields: String*): QueryBuilder = {
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
  def sortBy(field: String, order: Order): QueryBuilder = {
    solrQuery.addSortField(field, order)
    this
  }

  /**
   * Sets grouping field names.
   *
   * @param grouping field names
   */
  def groupBy(fields: String*): QueryBuilder = {
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
  def facetFields(fields: String*): QueryBuilder = {
    solrQuery.setFacet(true)
    solrQuery.addFacetField(fields: _*)
    this
  }

  /**
   * Specifies the maximum number of results to return.
   * 
   * @param rows number of results
   */
  def rows(rows: Int) = {
    solrQuery.setRows(rows)
    this
  }
    
  /**
   * Sets the offset to start at in the result set.
   * 
   * @param start zero-based offset
   */
  def start(start: Int) = {
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
  def highlight(field: String, size: Int = 100, prefix: String = "", postfix: String = "") = {
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
   * Returns the search result of this query as List[Map[String, Any]].
   *
   * @param params the parameter map or case class which would be given to the query
   * @return the search result
   */
  def getResultAsMap(params: Any = null): MapQueryResult = {

    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))

    val response = server.query(solrQuery)
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

    val queryResult = solrQuery.getParams("group") match {
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

    val facetResult = response.getFacetFields() match {
      case null => Map.empty[String, Map[String, Long]]
      case facetFields => facetFields.asScala.map { field => (
          field.getName(),
          field.getValues().asScala.map { value => (value.getName(), value.getCount()) }.toMap
      )}.toMap
    }

    MapQueryResult(response.getResults().getNumFound(), queryResult, facetResult)
  }

  /**
   * Returns the search result of this query as the case class.
   *
   * @param params the parameter map or case class which would be given to the query
   * @return the search result
   */
  def getResultAs[T](params: Any = null)(implicit m: Manifest[T]): CaseClassQueryResult[T] = {
    val result = getResultAsMap(params)

    CaseClassQueryResult[T](
      result.numFound,
      result.documents.map { doc =>
        CaseClassMapper.map2class[T](doc)
      },
      result.facetFields
    )

  }

}

