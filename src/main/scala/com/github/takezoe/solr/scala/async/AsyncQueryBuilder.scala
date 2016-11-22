package com.github.takezoe.solr.scala.async

import com.github.takezoe.solr.scala.async.AsyncUtils._
import com.github.takezoe.solr.scala.query.ExpressionParser
import org.asynchttpclient._
import org.apache.http.HttpStatus
import org.apache.solr.client.solrj.impl.{StreamingBinaryResponseParser, XMLResponseParser}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.{ResponseParser, StreamingResponseCallback}
import org.apache.solr.common.params.{CommonParams, ModifiableSolrParams, SolrParams}
import org.asynchttpclient.AsyncHandler.State

import scala.concurrent._
import scala.util.{Success, Failure}
import scala.util.control.Exception.ultimately

class AsyncQueryBuilder(httpClient: AsyncHttpClient, url: String, protected val query: String)
                       (implicit parser: ExpressionParser) extends AbstractAsyncQueryBuilder(query) {
    private def postQueryBody(params: SolrParams, respParser: ResponseParser) = {
      val wParams = new ModifiableSolrParams(params)
      wParams.set(CommonParams.WT, respParser.getWriterType)
      wParams.set(CommonParams.VERSION, respParser.getVersion)

      val qStr = wParams.toQueryString
      (if (qStr.charAt(0) == '?') qStr drop 1 else qStr) getBytes "UTF-8"
    }

    protected def createCopy = new AsyncQueryBuilder(httpClient, url, query)(parser)

    protected def query[T](solrQuery: SolrParams, success: QueryResponse => T): Future[T] = {
    val promise = Promise[T]()

    val parser = new XMLResponseParser

    httpClient.preparePost(url + "/select")
      .setHeader("Content-Type", "application/x-www-form-urlencoded")
      .setBody(postQueryBody(solrQuery, parser))
      .execute(new CallbackHandler(httpClient, promise, (response: Response) => {
          val namedList = parser.processResponse(response.getResponseBodyAsStream, "UTF-8")
          val queryResponse = new QueryResponse
          queryResponse.setResponse(namedList)
          success(queryResponse)
        }))
    
    promise.future
  }

  protected def stream[T](solrQuery: SolrParams, cb: StreamingResponseCallback, success: QueryResponse ⇒ T)
                         (implicit ec: ExecutionContext): Future[T] = {
    val respParser = new StreamingBinaryResponseParser(cb)

    val reqBuilder = httpClient preparePost s"$url/select" setHeader
      ("Content-Type", "application/x-www-form-urlencoded") setBody postQueryBody(solrQuery, respParser)

    val p = Promise[T]()
    reqBuilder.execute(new AsyncHandler[Unit] {
      val updInputStream = new UpdatableInputStream

      override def onThrowable(t: Throwable): Unit = p failure t

      override def onCompleted(): Unit = updInputStream.finishedAppending()

      override def onStatusReceived(responseStatus: HttpResponseStatus): State = {
        responseStatus.getStatusCode match {
          case HttpStatus.SC_OK ⇒
            Future(
              ultimately(updInputStream.close()) {
                val namedList = respParser.processResponse(updInputStream, null)
                val queryResponse = new QueryResponse
                queryResponse setResponse namedList
                success(queryResponse)
              }
            ).onComplete {
              case Success(r) => p success r
              case Failure(t) => p failure t
            }

            State.CONTINUE
          case s ⇒
            updInputStream.close()
            p failure new Exception(s"$s: ${responseStatus.getStatusText}")
            State.ABORT
        }
      }

      override def onHeadersReceived(headers: HttpResponseHeaders): State = State.CONTINUE

      override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): State = {
        updInputStream appendBytes bodyPart.getBodyPartBytes
        State.CONTINUE
      }
    })

    p.future
  }
}
