package jp.sf.amateras.solr.scala.async

import jp.sf.amateras.solr.scala.query.{ExpressionParser, QueryTemplate}
import jp.sf.amateras.solr.scala.{QueryBuilderBase, CaseClassMapper, CaseClassQueryResult, MapQueryResult}
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import scala.concurrent.Future

abstract class AbstractAsyncQueryBuilder(query: String)(implicit parser: ExpressionParser) extends QueryBuilderBase(query) {
    def getResultAsMap(params: Any = null): Future[MapQueryResult] = {
        solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
        query(solrQuery, { response => responseToMap(response) })
    }

    def getResultAs[T](params: Any = null)(implicit m: Manifest[T]): Future[CaseClassQueryResult[T]] = {
        solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
        query(solrQuery, { response => responseToObject[T](response) })
    }

    protected def query[T](solrQuery: SolrQuery, success: QueryResponse => T): Future[T]
}
