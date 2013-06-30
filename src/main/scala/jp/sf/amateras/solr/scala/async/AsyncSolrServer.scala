package jp.sf.amateras.solr.scala.async

import org.apache.solr.client.solrj.SolrQuery
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.AsyncCompletionHandler
import com.ning.http.client.Response

class AsyncSolrServer {
  
  def query[T](solrQuery: SolrQuery)(callback: () => T) = {
    println("----")
    println(solrQuery.getQuery)
    println("----")
    
    new AsyncHttpClient().prepareGet("http://localhost:8983/solr/select?" + solrQuery.getQuery)
      .execute(new AsyncCompletionHandler[T](){
        override def onCompleted(response: Response ): T = {
          callback()
        } 
      })
  }
  
}