package jp.sf.amateras.solr.scala.async

import org.apache.solr.client.solrj.SolrServer
import jp.sf.amateras.solr.scala._
import jp.sf.amateras.solr.scala.query._

class AsyncQueryBuilder(server: AsyncSolrServer, query: String)(implicit parser: ExpressionParser) extends QueryBuilderBase(query) {

  def getResultAsMap(params: Any = null)(callback: MapQueryResult => Unit): Unit = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    server.query(solrQuery){ response =>
      callback(responseToMap(response))
    }
  }

  def getResultAs[T](params: Any = null)(callback: CaseClassQueryResult[T] => Unit)(implicit m: Manifest[T]): Unit = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    server.query(solrQuery){ response =>
      callback(responseToObject[T](response))
    }
  }
  
}