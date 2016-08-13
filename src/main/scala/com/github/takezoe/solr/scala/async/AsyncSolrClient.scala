package com.github.takezoe.solr.scala.async

import com.ning.http.client.AsyncHttpClient
import java.net.URLEncoder

import AsyncUtils.CallbackHandler
import com.github.takezoe.solr.scala.query.{DefaultExpressionParser, ExpressionParser}
import org.apache.solr.client.solrj.request.{AbstractUpdateRequest, UpdateRequest}
import scala.concurrent._

/**
 * Provides the asynchronous and non-blocking API for Solr.
 */
class AsyncSolrClient(url: String, factory: () => AsyncHttpClient = { () => new AsyncHttpClient() })
    (implicit protected val parser: ExpressionParser = new DefaultExpressionParser())
    extends IAsyncSolrClient {
  
  val httpClient: AsyncHttpClient = factory()
  
  /**
   * Search documents using the given query.
   */
  def query(query: String) = new AsyncQueryBuilder(httpClient, url, query)

  /**
   * Commit the current session.
   */
  def commit(): Future[Unit] = {
    val req = new UpdateRequest()
    req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true)
    execute(httpClient, req, Promise[Unit]())
  }
  
  /**
   * Rolled back the current session.
   */
  def rollback(): Future[Unit] = {
    val req = new UpdateRequest().rollback().asInstanceOf[UpdateRequest]
    execute(httpClient, req, Promise[Unit]())
  }

  /**
   * Shutdown AsyncHttpClient.
   * Call this method before stopping your application.
   */
  def shutdown(): Unit = httpClient.closeAsynchronously()

    override protected def execute(req: UpdateRequest, promise: Promise[Unit]): Future[Unit] = {
        execute(httpClient, req, promise)
    }

    /**
   * Send request using AsyncHttpClient.
   * Returns the result of asynchronous request to the future world via given Promise.
   */
  private def execute(httpClient: AsyncHttpClient, req: UpdateRequest, promise: Promise[Unit]): Future[Unit] = {
    
    val builder = httpClient.preparePost(url + "/update")

    if(req.getXML != null){
      // Send XML as request body
      val writer = new java.io.StringWriter()
      req.writeXML(writer)
      builder.addHeader("Content-Type", "text/xml; charset=UTF-8")
      builder.setBody(writer.toString.getBytes("UTF-8"))
      
    } else {
      // Send URL encoded parameters as request body
      builder.addHeader("Content-Charset", "UTF-8")
      builder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        import scala.collection.JavaConverters._
        val params = req.getParams
      builder.setBody(params.getParameterNames.asScala.map { name =>
        URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(params.get(name), "UTF-8")
      }.mkString("&"))
    }
    
    builder.execute(new CallbackHandler(httpClient, promise))
    
    promise.future
  }
  
}
