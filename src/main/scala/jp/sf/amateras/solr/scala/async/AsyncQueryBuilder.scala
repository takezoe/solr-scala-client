package jp.sf.amateras.solr.scala.async

import AsyncUtils._
import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client._
import jp.sf.amateras.solr.scala.query._
import org.apache.http.HttpStatus
import org.apache.solr.client.solrj.{ResponseParser, StreamingResponseCallback}
import org.apache.solr.client.solrj.impl.{StreamingBinaryResponseParser, XMLResponseParser}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.util.ClientUtils
import org.apache.solr.common.params.{CommonParams, ModifiableSolrParams, SolrParams}
import scala.concurrent._
import scala.util.control.Exception.ultimately

class AsyncQueryBuilder(httpClient: AsyncHttpClient, url: String, protected val query: String)
                       (implicit parser: ExpressionParser) extends AbstractAsyncQueryBuilder(query) {
    private def postQueryBody(params: SolrParams, respParser: ResponseParser) = {
      val wParams = new ModifiableSolrParams(params)
      wParams.set(CommonParams.WT, respParser.getWriterType)
      wParams.set(CommonParams.VERSION, respParser.getVersion)

      val qStr = ClientUtils.toQueryString(wParams, false)
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

  protected def stream[T](solrQuery: SolrParams, cb: StreamingResponseCallback, success: QueryResponse ⇒ T): Future[T] = {
    val respParser = new StreamingBinaryResponseParser(cb)

    val reqBuilder = httpClient preparePost s"$url/select" setHeader
      ("Content-Type", "application/x-www-form-urlencoded") setBody postQueryBody(solrQuery, respParser)

    val p = Promise[T]()
    reqBuilder.execute(new AsyncHandler[Unit] {
      val updInputStream = new UpdatableInputStream

      override def onThrowable(t: Throwable): Unit = p failure t

      override def onCompleted(): Unit = updInputStream.finishedAppending()

      override def onStatusReceived(responseStatus: HttpResponseStatus): STATE = {
        responseStatus.getStatusCode match {
          case HttpStatus.SC_OK ⇒
            import scala.concurrent.ExecutionContext.Implicits.global

            Future(blocking {
              ultimately(updInputStream.close()) {
                val namedList = respParser.processResponse(updInputStream, null)
                val queryResponse = new QueryResponse
                queryResponse setResponse namedList
                p success success(queryResponse)
              }
            }) onFailure {
              case t ⇒ p failure t
            }

            STATE.CONTINUE
          case s ⇒
            updInputStream.close()
            p failure new Exception(s"$s: ${responseStatus.getStatusText}")
            STATE.ABORT
        }
      }

      override def onHeadersReceived(headers: HttpResponseHeaders): STATE = STATE.CONTINUE

      override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): STATE = {
        updInputStream appendBytes bodyPart.getBodyPartBytes
        STATE.CONTINUE
      }
    })

    p.future
  }
}
