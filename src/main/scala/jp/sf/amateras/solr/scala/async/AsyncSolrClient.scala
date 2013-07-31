package jp.sf.amateras.solr.scala.async

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import AsyncUtils._
import jp.sf.amateras.solr.scala.query._
import jp.sf.amateras.solr.scala.CaseClassMapper
import com.ning.http.client.AsyncHttpClient
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.request.UpdateRequest
import org.apache.solr.client.solrj.request.AbstractUpdateRequest
import org.apache.solr.client.solrj.response.QueryResponse
import java.net.URLEncoder
import scala.util.Success
import scala.util.Failure

/**
 * Provides the asynchronous and non-blocking API for Solr.
 */
class AsyncSolrClient(url: String, factory: () => AsyncHttpClient = { () => new AsyncHttpClient() })
    (implicit parser: ExpressionParser = new DefaultExpressionParser()) {
  
  val httpClient: AsyncHttpClient = factory()
  
  def withTransaction[T](operations: (AsyncSolrClient) => Future[T]): Future[T] = {
    val future = operations(this)
    future.onComplete {
      case Success(x) => commit
      case Failure(t) => rollback
    }
    future
  }
  
  def query(query: String): AsyncQueryBuilder = new AsyncQueryBuilder(httpClient, url, query)
  
  /**
   * Registers the document and commit immediately.
   */
  def register(doc: Any): Future[Unit] = {
    val solrDoc = new SolrInputDocument
    CaseClassMapper.toMap(doc).map { case (key, value) =>
      solrDoc.addField(key, value)
    }

    val req = new UpdateRequest()
    req.add(solrDoc)
      
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
      
    withTransaction { _ =>
      promise.future
    }
  }
  
  /**
   * Deletes the document by id and commit immediately.
   */
  def deleteById(id: String): Future[Unit] = {
    val req = new UpdateRequest()
    req.deleteById(id)
    
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    withTransaction { _ =>
      promise.future
    }
  }
  
  /**
   * Commit the current session.
   */
  def commit(): Future[Unit] = {
    val req = new UpdateRequest()
    req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true)
    
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    promise.future
  }
  
  /**
   * Rolled back the current session.
   */
  def rollback(): Future[Unit] = {
    val req = new UpdateRequest().rollback().asInstanceOf[UpdateRequest]
    
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    promise.future
  }

  /**
   * Shutdown AsyncHttpClient.
   * Call this method before stopping your application.
   */
  def shutdown(): Unit = httpClient.closeAsynchronously()
  
  /**
   * Send request using AsyncHttpClient.
   * Returns the result of asynchronous request to the future world via given Promise.
   */
  private def execute(httpClient: AsyncHttpClient, req: UpdateRequest, promise: Promise[Unit]): Unit = {
    
    val builder = httpClient.preparePost(url + "/update")

    if(req.getXML != null){
      // Send XML as request body
      val writer = new java.io.StringWriter()
      req.writeXML(writer)
      builder.addHeader("Content-Type", "text/xml; charset=UTF-8")
      builder.setBody(writer.toString.getBytes("UTF-8"))
      
    } else {
      // Send URL encoded parameters as request body
      builder.addHeader("Content-Charset", "UTF-8");
      builder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
      import scala.collection.JavaConverters._
      val params = req.getParams()
      builder.setBody(params.getParameterNames.asScala.map { name =>
        URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(params.get(name), "UTF-8")
      }.mkString("&"))
    }
    
    builder.execute(new CallbackHandler(httpClient, promise))
  }
  
}