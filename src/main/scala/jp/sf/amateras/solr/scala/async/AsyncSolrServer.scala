package jp.sf.amateras.solr.scala.async

import java.io.StringWriter
import java.net.URLEncoder

import scala.concurrent.Future
import scala.concurrent.Promise

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.request.UpdateRequest
import org.apache.solr.client.solrj.response.QueryResponse

import com.ning.http.client._

import AsyncUtils._

class AsyncSolrServer(url: String, factory: () => AsyncHttpClient = { () => new AsyncHttpClient() }) {
  
  val httpClient = factory()
    
  def query[T](solrQuery: SolrQuery, success: QueryResponse => T): Future[T] = {
    val promise = Promise[T]()
    
    httpClient.prepareGet(url + "/select?q=" + URLEncoder.encode(solrQuery.getQuery, "UTF-8") + "&wt=xml")
      .execute(new CallbackHandler(httpClient, promise, (response: Response) => {
          val parser = new XMLResponseParser
          val namedList = parser.processResponse(response.getResponseBodyAsStream, "UTF-8")
          val queryResponse = new QueryResponse
          queryResponse.setResponse(namedList)
          success(queryResponse)
        }))
    
    promise.future
  }
  
  def deleteById(id: String): Future[Unit] = {
    val req = new UpdateRequest()
    req.deleteById(id)
    req.setCommitWithin(1000) // TODO 
    
   val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    promise.future;
  }
  
  def commit(): Future[Unit] = {
    val req = new UpdateRequest()
    req.setCommitWithin(0)
    
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    promise.future;
  }
  
  private def execute(httpClient: AsyncHttpClient, req: UpdateRequest, promise: Promise[Unit]): Unit = {
    val writer = new java.io.StringWriter()
    req.writeXML(writer)
    
    val builder = httpClient.preparePost(url + "/update")
    builder.addHeader("Content-Type", "text/xml; charset=UTF-8")
    builder.setBody(writer.toString.getBytes("UTF-8"))
    builder.execute(new CallbackHandler(httpClient, promise))
  }

  def shutdown(): Unit = {
    httpClient.closeAsynchronously()
  }
  
}

