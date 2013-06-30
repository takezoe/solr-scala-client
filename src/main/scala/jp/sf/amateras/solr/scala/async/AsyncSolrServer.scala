package jp.sf.amateras.solr.scala.async

import org.apache.solr.client.solrj.SolrQuery
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.AsyncCompletionHandler
import com.ning.http.client.Response
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.response.QueryResponse

class AsyncSolrServer {
  
  def query[T](solrQuery: SolrQuery)(callback: () => T) = {
    println("----")
    println(solrQuery.getQuery)
    println("----")
    
    new AsyncHttpClient().prepareGet("http://localhost:8983/solr/select?" + solrQuery.getQuery)
      .execute(new AsyncCompletionHandler[T](){
        override def onCompleted(response: Response): T = {
          val parser = new XMLResponseParser
          val namedList = parser.processResponse(response.getResponseBodyAsStream, "UTF-8")
          val queryResponse = new QueryResponse
          queryResponse.setResponse(namedList)
          
          println(queryResponse.getResults().getNumFound())
          
          callback()
        } 
      })
  }
  
}

object AsyncTest extends App {
  val query = new SolrQuery()
  query.setQuery("*:*")
  
  val server = new AsyncSolrServer()
  server.query(query){() =>
    println("OK!!")
  }
 
}