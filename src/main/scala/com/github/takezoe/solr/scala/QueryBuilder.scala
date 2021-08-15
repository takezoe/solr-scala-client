package com.github.takezoe.solr.scala

import com.github.takezoe.solr.scala.query.{ExpressionParser, QueryTemplate}
import org.apache.solr.client.solrj.{SolrClient => ApacheSolrClient}

import scala.reflect.ClassTag

class QueryBuilder(server: ApacheSolrClient, query: String)(implicit parser: ExpressionParser)
  extends QueryBuilderBase[QueryBuilder] {

  protected def createCopy = new QueryBuilder(server, query)(parser)

  /**
   * Returns the search result of this query as List[Map[String, Any]].
   *
   * @param params the parameter map or case class which would be given to the query
   * @return the search result
   */
  def getResultAsMap(params: Any = null): MapQueryResult = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    responseToMap(server.query(collection, solrQuery))
  }

  /**
   * Returns the search result of this query as the case class.
   *
   * @param params the parameter map or case class which would be given to the query
   * @return the search result
   */
  def getResultAs[T](params: Any = null)(implicit m: ClassTag[T]): CaseClassQueryResult[T] = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    responseToObject[T](server.query(collection, solrQuery))
  }

}
