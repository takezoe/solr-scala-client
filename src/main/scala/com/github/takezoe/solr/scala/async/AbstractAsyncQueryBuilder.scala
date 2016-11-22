package com.github.takezoe.solr.scala.async

import java.lang

import com.github.takezoe.solr.scala._
import com.github.takezoe.solr.scala.query._
import com.github.takezoe.solr.scala.async.AbstractAsyncQueryBuilder.StreamingCallback
import org.apache.solr.client.solrj.StreamingResponseCallback
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.params.SolrParams

import scala.concurrent.{ExecutionContext, Future}

object AbstractAsyncQueryBuilder {
  abstract class StreamingCallback[T] {
    def streamDocument(doc: T): Unit

    def streamDocListInfo(numFound: Long, start: Long, maxScore: java.lang.Float): Unit
  }
}

abstract class AbstractAsyncQueryBuilder(query: String)(implicit parser: ExpressionParser)
    extends QueryBuilderBase[AbstractAsyncQueryBuilder] {

    def getResultAsMap(params: Any = null): Future[MapQueryResult] = {
        solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
        query(solrQuery, { response => responseToMap(response) })
    }

    def getResultAs[T](params: Any = null)(implicit m: Manifest[T]): Future[CaseClassQueryResult[T]] = {
        solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
        query(solrQuery, { response => responseToObject[T](response) })
    }

    def streamResultsAsMap(cb: StreamingCallback[DocumentMap], params: Any = null)(implicit ex: ExecutionContext): Future[MapQueryResult] = {
        solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
        stream(solrQuery, new StreamingResponseCallback {
          override def streamSolrDocument(doc: SolrDocument): Unit = {
            cb streamDocument docToMap(doc)
          }

          override def streamDocListInfo(numFound: Long, start: Long, maxScore: lang.Float): Unit = {
            cb.streamDocListInfo(numFound, start, maxScore)
          }
        }, { response ⇒ responseToMap(response) })
    }

    def streamResultsAs[T](cb: StreamingCallback[T], params: Any = null)
                          (implicit m: Manifest[T], ec: ExecutionContext): Future[CaseClassQueryResult[T]] = {
      solrQuery.setQuery(new QueryTemplate(query).merge(CaseClassMapper.toMap(params)))
      stream(solrQuery, new StreamingResponseCallback {
        override def streamSolrDocument(doc: SolrDocument): Unit = {
          cb streamDocument CaseClassMapper.map2class[T](docToMap(doc))
        }

        override def streamDocListInfo(numFound: Long, start: Long, maxScore: lang.Float): Unit = {
          cb.streamDocListInfo(numFound, start, maxScore)
        }
      }, { response ⇒ responseToObject[T](response) })
    }

    protected def query[T](solrQuery: SolrParams, success: QueryResponse => T): Future[T]

    protected def stream[T](solrQuery: SolrParams, cb: StreamingResponseCallback, success: QueryResponse ⇒ T)
                           (implicit ex: ExecutionContext): Future[T]
}
