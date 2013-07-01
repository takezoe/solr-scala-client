package jp.sf.amateras.solr.scala.async

import jp.sf.amateras.solr.scala.query._

class AsyncSolrClient(url: String)
    (implicit factory: (String) => AsyncSolrServer = { (url: String) => new AsyncSolrServer(url) }, 
            parser: ExpressionParser = new DefaultExpressionParser()) {
  
  val server = factory(url)
  
  def query(query: String): AsyncQueryBuilder = new AsyncQueryBuilder(server, query)

}