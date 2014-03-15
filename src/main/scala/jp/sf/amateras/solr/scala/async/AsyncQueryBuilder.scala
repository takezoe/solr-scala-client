package jp.sf.amateras.solr.scala.async

import AsyncUtils._
import com.ning.http.client._
import jp.sf.amateras.solr.scala.query._
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.util.ClientUtils
import org.apache.solr.common.params.SolrParams
import scala.concurrent._

class AsyncQueryBuilder(httpClient: AsyncHttpClient, url: String, protected val query: String)
                       (implicit parser: ExpressionParser) extends AbstractAsyncQueryBuilder(query) {

    protected def createCopy = new AsyncQueryBuilder(httpClient, url, query)(parser)

    protected def query[T](solrQuery: SolrParams, success: QueryResponse => T): Future[T] = {
    val promise = Promise[T]()
    
    httpClient.preparePost(url + "/select")
      .setHeader("Content-Type", "application/x-www-form-urlencoded")
      .setBody((ClientUtils.toQueryString(solrQuery, false) + "&wt=xml").getBytes("UTF-8"))
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
