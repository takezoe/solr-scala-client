package jp.sf.amateras.solr.scala.async

import AsyncUtils._
import com.ning.http.client._
import java.net.URLEncoder
import jp.sf.amateras.solr.scala.query._
import org.apache.solr.client.solrj._
import org.apache.solr.client.solrj.impl.XMLResponseParser
import org.apache.solr.client.solrj.response.QueryResponse
import scala.concurrent._

class AsyncQueryBuilder(httpClient: AsyncHttpClient, url: String, protected val query: String)
                       (implicit parser: ExpressionParser) extends AbstractAsyncQueryBuilder(query) {

    protected def query[T](solrQuery: SolrQuery, success: QueryResponse => T): Future[T] = {
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
}
