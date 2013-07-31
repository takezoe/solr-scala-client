package jp.sf.amateras.solr.scala.async

import scala.concurrent._
import org.apache.solr.client.solrj._
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.impl.XMLResponseParser
import jp.sf.amateras.solr.scala._
import jp.sf.amateras.solr.scala.query._
import AsyncUtils._
import java.net.URLEncoder
import com.ning.http.client._

class AsyncQueryBuilder(httpClient: AsyncHttpClient, url: String, query: String)(implicit parser: ExpressionParser) extends QueryBuilderBase(query) {

  def getResultAsMap(params: Any = null): Future[MapQueryResult] = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    query(solrQuery, { response => responseToMap(response) })
  }
  
  def getResultAs[T](params: Any = null)(implicit m: Manifest[T]): Future[CaseClassQueryResult[T]] = {
    solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
    query(solrQuery, { response => responseToObject[T](response) })
  }
  
  private def query[T](solrQuery: SolrQuery, success: QueryResponse => T): Future[T] = {
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