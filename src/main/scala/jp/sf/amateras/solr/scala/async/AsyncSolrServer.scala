package jp.sf.amateras.solr.scala.async

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.response.QueryResponse
import com.ning.http.client._
import java.net.URLEncoder
import org.apache.solr.client.solrj.request.UpdateRequest
import java.io.StringWriter
import AsyncUtils._

class AsyncSolrServer(url: String, factory: () => AsyncHttpClient = { () => new AsyncHttpClient() }) {
  
  def query(solrQuery: SolrQuery, success: QueryResponse => Unit, failure: Throwable => Unit): Unit = {
    val httpClient = factory()
    httpClient.prepareGet(url + "/select?q=" + URLEncoder.encode(solrQuery.getQuery, "UTF-8") + "&wt=xml")
      .execute(new CallbackHandler(httpClient, (response: Response) => {
          val parser = new XMLResponseParser
          val namedList = parser.processResponse(response.getResponseBodyAsStream, "UTF-8")
          val queryResponse = new QueryResponse
          queryResponse.setResponse(namedList)
          success(queryResponse)
        }, failure))
  }
  
  def deleteById(id: String, failure: Throwable => Unit): Unit = {
    val req = new UpdateRequest()
    req.deleteById(id)
    req.setCommitWithin(1000) // TODO 
    
    val httpClient = factory()
    execute(httpClient, req, new CallbackHandler(httpClient, defaultSuccessHandler, failure))
  }
  
  private def execute(httpClient: AsyncHttpClient, req: UpdateRequest, handler: AsyncHandler[Unit]): Unit = {
    val writer = new java.io.StringWriter()
    req.writeXML(writer)
    
    val builder = httpClient.preparePost(url + "/update")
    builder.addHeader("Content-Type", "text/xml; charset=UTF-8")
    builder.setBody(writer.toString.getBytes("UTF-8"))
    builder.execute(handler)
  }

}

