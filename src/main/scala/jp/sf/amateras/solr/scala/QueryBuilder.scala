package jp.sf.amateras.solr.scala

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.util.NamedList

import jp.sf.amateras.solr.scala.query.ExpressionParser
import jp.sf.amateras.solr.scala.query.QueryTemplate
import org.apache.solr.client.solrj.response.QueryResponse

class QueryBuilder(server: SolrServer, query: String)(implicit parser: ExpressionParser) extends QueryBuilderBase(query) {

  /**
   * Returns the search result of this query as List[Map[String, Any]].
   *
   * @param params the parameter map or case class which would be given to the query
   * @return the search result
   */
  def getResultAsMap(params: Any = null): MapQueryResult = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    responseToMap(server.query(solrQuery))
  }

  /**
   * Returns the search result of this query as the case class.
   *
   * @param params the parameter map or case class which would be given to the query
   * @return the search result
   */
  def getResultAs[T](params: Any = null)(implicit m: Manifest[T]): CaseClassQueryResult[T] = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    responseToObject[T](server.query(solrQuery))
  }

}

