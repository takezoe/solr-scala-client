package com.github.takezoe.solr.scala.async

import java.io.IOException

import com.github.takezoe.solr.scala.async.AsyncUtils._
import com.github.takezoe.solr.scala.query.ExpressionParser
import okhttp3._
import org.apache.http.HttpStatus
import org.apache.solr.client.solrj.impl.{StreamingBinaryResponseParser, XMLResponseParser}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.{ResponseParser, StreamingResponseCallback}
import org.apache.solr.common.params.{CommonParams, SolrParams}

import scala.concurrent._
import scala.util.{Failure, Success}
import scala.util.control.Exception.ultimately
import scala.collection.JavaConverters._

class AsyncQueryBuilder(httpClient: OkHttpClient, url: String, protected val query: String)
                       (implicit parser: ExpressionParser) extends AbstractAsyncQueryBuilder(query) {

    protected def createFormBody(solrQuery: SolrParams, parser: ResponseParser) = {
      val formBuilder = new FormBody.Builder()

      solrQuery.getParameterNamesIterator.asScala.foreach { name =>
        formBuilder.add(name, solrQuery.get(name))
      }
      formBuilder.add(CommonParams.WT, parser.getWriterType)
      formBuilder.add(CommonParams.VERSION, parser.getVersion)

      formBuilder.build()
    }

    protected def createCopy = new AsyncQueryBuilder(httpClient, url, query)(parser)

    protected def query[T](solrQuery: SolrParams, success: QueryResponse => T): Future[T] = {
    val promise = Promise[T]()

    val parser = new XMLResponseParser
    val builder = new Request.Builder().url(url + "/select").post(createFormBody(solrQuery, parser))

    httpClient.newCall(builder.build()).enqueue(new CallbackHandler(httpClient, promise, (response: Response) => {
      val namedList = parser.processResponse(response.body.byteStream, "UTF-8")
      val queryResponse = new QueryResponse
      queryResponse.setResponse(namedList)
      success(queryResponse)
    }))
    
    promise.future
  }

// TODO Is it possible to implement this method in OkHttp3?
//  protected def stream[T](solrQuery: SolrParams, cb: StreamingResponseCallback, success: QueryResponse ⇒ T)
//                         (implicit ec: ExecutionContext): Future[T] = {
//    val respParser = new StreamingBinaryResponseParser(cb)
//
//    val reqBuilder = httpClient preparePost s"$url/select" setHeader
//      ("Content-Type", "application/x-www-form-urlencoded") setBody postQueryBody(solrQuery, respParser)
//
//    val p = Promise[T]()
//    reqBuilder.execute(new AsyncHandler[Unit] {
//      val updInputStream = new UpdatableInputStream
//
//      override def onThrowable(t: Throwable): Unit = p failure t
//
//      override def onCompleted(): Unit = updInputStream.finishedAppending()
//
//      override def onStatusReceived(responseStatus: HttpResponseStatus): State = {
//        responseStatus.getStatusCode match {
//          case HttpStatus.SC_OK ⇒
//            Future(
//              ultimately(updInputStream.close()) {
//                val namedList = respParser.processResponse(updInputStream, null)
//                val queryResponse = new QueryResponse
//                queryResponse setResponse namedList
//                success(queryResponse)
//              }
//            ).onComplete {
//              case Success(r) => p success r
//              case Failure(t) => p failure t
//            }
//
//            State.CONTINUE
//          case s ⇒
//            updInputStream.close()
//            p failure new Exception(s"$s: ${responseStatus.getStatusText}")
//            State.ABORT
//        }
//      }
//
//      override def onHeadersReceived(headers: HttpResponseHeaders): State = State.CONTINUE
//
//      override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): State = {
//        updInputStream appendBytes bodyPart.getBodyPartBytes
//        State.CONTINUE
//      }
//    })
//
//    p.future
//  }

}
