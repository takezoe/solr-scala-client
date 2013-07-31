package jp.sf.amateras.solr.scala.async

import java.io.StringWriter
import java.net.URLEncoder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.request.UpdateRequest
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrInputDocument
import com.ning.http.client._
import AsyncUtils._
import jp.sf.amateras.solr.scala.CaseClassMapper
import org.apache.solr.client.solrj.request.AbstractUpdateRequest

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

  /**
   * Register document and commit immediately.
   */
  def register(doc: Any): Future[Unit] = {
    val solrDoc = new SolrInputDocument
    CaseClassMapper.toMap(doc).map { case (key, value) =>
      solrDoc.addField(key, value)
    }
    //server.add(solrDoc)
    val req = new UpdateRequest()
    req.add(solrDoc)
    
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    promise.future map { _ => commit }
  }
  
  /**
   * Delete document by id and commit immediately.
   */
  def deleteById(id: String): Future[Unit] = {
    val req = new UpdateRequest()
    req.deleteById(id)
    
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    promise.future map { _ => commit }
  }
  
  /**
   * Commit transaction.
   */
  def commit(): Future[Unit] = {
    val req = new UpdateRequest()
    req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true)
    
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    promise.future
  }
  
  def rollback(): Future[Unit] = {
    val req = new UpdateRequest().rollback().asInstanceOf[UpdateRequest]
    
    val promise = Promise[Unit]()
    execute(httpClient, req, promise)
    
    promise.future
  }
  
  private def execute(httpClient: AsyncHttpClient, req: UpdateRequest, promise: Promise[Unit]): Unit = {
    
    val builder = httpClient.preparePost(url + "/update")

    if(req.getXML != null){
      // Send XML as request body
      val writer = new java.io.StringWriter()
      req.writeXML(writer)
      builder.addHeader("Content-Type", "text/xml; charset=UTF-8")
      builder.setBody(writer.toString.getBytes("UTF-8"))
      
    } else {
      // Send parameters as request body
      builder.addHeader("Content-Charset", "UTF-8");
      builder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
      import scala.collection.JavaConverters._
      val params = req.getParams()
      builder.setBody(params.getParameterNames.asScala.map { name =>
        name + "=" + params.get(name)
      }.mkString("&"))
    }
    
    builder.execute(new CallbackHandler(httpClient, promise))
  }

  def shutdown(): Unit = {
    httpClient.closeAsynchronously()
  }
  
}

