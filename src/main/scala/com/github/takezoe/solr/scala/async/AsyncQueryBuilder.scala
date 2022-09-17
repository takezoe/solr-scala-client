package com.github.takezoe.solr.scala.async

import java.io.IOException

import com.github.takezoe.solr.scala.async.AsyncUtils._
import com.github.takezoe.solr.scala.query.ExpressionParser
import okhttp3._
import org.apache.solr.client.solrj.impl.{StreamingBinaryResponseParser, XMLResponseParser}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.{ResponseParser, StreamingResponseCallback}
import org.apache.solr.common.params.{CommonParams, SolrParams}

import scala.concurrent._
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

  protected def stream(solrQuery: SolrParams, cb: StreamingResponseCallback)(implicit ec: ExecutionContext): Future[Unit] = {
    val promise = Promise[Unit]()

    val parser = new StreamingBinaryResponseParser(cb)
    val builder = new Request.Builder().url(url + "/select").post(createFormBody(solrQuery, parser))

    httpClient.newCall(builder.build()).enqueue(new Callback {
      override def onFailure(call: Call, e: IOException): Unit = {
        promise.failure(e)
      }
      override def onResponse(call: Call, response: Response): Unit = try {
        val namedList = parser.processResponse(response.body.byteStream, "UTF-8")
        val queryResponse = new QueryResponse
        queryResponse.setResponse(namedList)
        promise.success((): Unit)
      } finally {
        response.close()
      }
    })

    promise.future
  }

}
