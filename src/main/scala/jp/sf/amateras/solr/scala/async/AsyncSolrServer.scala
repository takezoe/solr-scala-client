package jp.sf.amateras.solr.scala.async

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.response.QueryResponse

import com.ning.http.client._

import java.net.URLEncoder

class AsyncSolrServer(url: String, httpClient: AsyncHttpClient = new AsyncHttpClient()) {
  
  def query(solrQuery: SolrQuery)(success: QueryResponse => Unit)(failure: Throwable => Unit) = {
    httpClient.prepareGet(url + "/select?q=" + URLEncoder.encode(solrQuery.getQuery, "UTF-8") + "&wt=xml")
      .execute(new AsyncCompletionHandler[Unit](){
        override def onCompleted(response: Response): Unit = {
          try {
            val parser = new XMLResponseParser
            val namedList = parser.processResponse(response.getResponseBodyAsStream, "UTF-8")
            val queryResponse = new QueryResponse
            queryResponse.setResponse(namedList)
            success(queryResponse)
          } finally {
            httpClient.close()
          }
        }
        
        override def onThrowable(t: Throwable): Unit = {
          try {
            failure(t)
          } finally {
            httpClient.close()
          }
        }
      })
  }
  
}
