package jp.sf.amateras.solr.scala.async

import AsyncUtils._
import com.ning.http.client._
import jp.sf.amateras.solr.scala.query._
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.util.ClientUtils
import org.apache.solr.common.params.{CommonParams, ModifiableSolrParams, SolrParams}
import scala.concurrent._

class AsyncQueryBuilder(httpClient: AsyncHttpClient, url: String, protected val query: String)
                       (implicit parser: ExpressionParser) extends AbstractAsyncQueryBuilder(query) {
    private def postQueryBody(params: SolrParams) = {
        val qStr = ClientUtils.toQueryString(params, false)
        (if (qStr.charAt(0) == '?') qStr drop 1 else qStr) getBytes "UTF-8"
    }

    protected def createCopy = new AsyncQueryBuilder(httpClient, url, query)(parser)

    protected def query[T](solrQuery: SolrParams, success: QueryResponse => T): Future[T] = {
    val promise = Promise[T]()

    val wParams = new ModifiableSolrParams(solrQuery)
    wParams.set(CommonParams.WT, "xml")

    httpClient.preparePost(url + "/select")
      .setHeader("Content-Type", "application/x-www-form-urlencoded")
      .setBody(postQueryBody(wParams))
      .execute(new CallbackHandler(httpClient, promise, (response: Response) => {
          val parser = new XMLResponseParser
          val namedList = parser.processResponse(response.getResponseBodyAsStream, "UTF-8")
          val queryResponse = new QueryResponse
          queryResponse.setResponse(namedList)
          success(queryResponse)
        }))
    
    promise.future
  }
}
