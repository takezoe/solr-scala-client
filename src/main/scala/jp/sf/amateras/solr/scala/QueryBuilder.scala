package jp.sf.amateras.solr.scala

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrDocumentList

class QueryBuilder(server: SolrServer, query: String) {

  private val solrQuery = new SolrQuery()

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
   * Returns the search result of this query as List[Map[String, Any]].
   *
   * @param params the parameter map which would be given to the query
   * @return the search result
   */
  def getResult(params: Map[String, Any] = Map()): QueryResult = {

    def toList(docList: SolrDocumentList): List[Map[String, Any]] = {
      (for(i <- 0 to docList.size() - 1) yield {
        val doc = docList.get(i)
        doc.getFieldNames().asScala.map { key => (key, doc.getFieldValue(key)) }.toMap
      }).toList
    }

    solrQuery.setQuery(new QueryTemplate(query).merge(params))

    val response = server.query(solrQuery)

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

    val facetResult =
      response.getFacetFields().asScala.map { field => (
        field.getName(),
        field.getValues().asScala.map { value => (value.getName(), value.getCount()) }.toMap
      )}.toMap

    QueryResult(queryResult, facetResult)
  }

}