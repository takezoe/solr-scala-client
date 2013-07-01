package jp.sf.amateras.solr.scala.async

import org.apache.solr.client.solrj.SolrServer
import jp.sf.amateras.solr.scala._
import jp.sf.amateras.solr.scala.query._

class AsyncQueryBuilder(server: AsyncSolrServer, query: String)(implicit parser: ExpressionParser) extends QueryBuilderBase(query) {

  def getResultAsMap(params: Any = null)
      (success: MapQueryResult => Unit, failure: Throwable => Unit = defaultErrorHandler): Unit = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    server.query(solrQuery){ response =>
      success(responseToMap(response))
    }(failure)
  }

  def getResultAs[T](params: Any = null)
      (success: CaseClassQueryResult[T] => Unit, failure: Throwable => Unit = defaultErrorHandler)(implicit m: Manifest[T]): Unit = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    server.query(solrQuery){ response =>
      success(responseToObject[T](response))
    }(failure)
  }
  
  def defaultErrorHandler(t: Throwable): Unit = t.printStackTrace
  
}