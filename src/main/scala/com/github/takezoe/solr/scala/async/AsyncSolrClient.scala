package com.github.takezoe.solr.scala.async

import AsyncUtils.CallbackHandler
import com.github.takezoe.solr.scala.query.{DefaultExpressionParser, ExpressionParser}
import okhttp3._
import org.apache.solr.client.solrj.request.{AbstractUpdateRequest, UpdateRequest}

import scala.concurrent._

object ContentTypes {
  val XML = MediaType.parse("text/xml; charset=UTF-8")
}

/**
 * Provides the asynchronous and non-blocking API for Solr.
 */
class AsyncSolrClient(url: String, factory: () => OkHttpClient = { () => new OkHttpClient() })
    (implicit protected val parser: ExpressionParser = new DefaultExpressionParser())
    extends IAsyncSolrClient {

  private val httpClient: OkHttpClient = factory()
  private val normalizedUrl = if(url.endsWith("/")) url.substring(0, url.length - 1) else url

  /**
   * Search documents using the given query.
   */
  def query(query: String) = new AsyncQueryBuilder(httpClient, normalizedUrl, query)

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
  def shutdown(): Unit = httpClient.dispatcher.executorService.shutdown()

    override protected def execute(req: UpdateRequest, promise: Promise[Unit]): Future[Unit] = {
        execute(httpClient, req, promise)
    }

  /**
   * Send request using AsyncHttpClient.
   * Returns the result of asynchronous request to the future world via given Promise.
   */
  private def execute(httpClient: OkHttpClient, req: UpdateRequest, promise: Promise[Unit]): Future[Unit] = {

    //val builder = httpClient.preparePost(normalizedUrl + "/update")
    val builder = new Request.Builder().url(normalizedUrl + "/update")

    if(req.getXML != null){
      // Send XML as request body
      val writer = new java.io.StringWriter()
      req.writeXML(writer)
      builder.post(RequestBody.create(ContentTypes.XML, writer.toString))

    } else {
      // Send URL encoded parameters as request body
      builder.addHeader("Content-Charset", "UTF-8")
      builder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
      import scala.collection.JavaConverters._
      val params = req.getParams

      val formBuilder = new FormBody.Builder()
      params.getParameterNames.asScala.map { name =>
        formBuilder.add(name, params.get(name))
      }
      builder.post(formBuilder.build())
    }

    val request = builder.build()
    httpClient.newCall(request).enqueue(new CallbackHandler(httpClient, promise))

    promise.future
  }

}
