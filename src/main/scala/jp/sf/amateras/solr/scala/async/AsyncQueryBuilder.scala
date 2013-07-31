package jp.sf.amateras.solr.scala.async

import scala.concurrent._
import org.apache.solr.client.solrj.SolrServer
import jp.sf.amateras.solr.scala._
import jp.sf.amateras.solr.scala.query._
import AsyncUtils._

class AsyncQueryBuilder(server: AsyncSolrServer, query: String)(implicit parser: ExpressionParser) extends QueryBuilderBase(query) {

  def getResultAsMap(params: Any = null): Future[MapQueryResult] = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    server.query(solrQuery, { response => responseToMap(response) })
  }
  
  def getResultAs[T](params: Any = null)(implicit m: Manifest[T]): Future[CaseClassQueryResult[T]] = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    server.query(solrQuery, { response => responseToObject[T](response) })
  }
  
}