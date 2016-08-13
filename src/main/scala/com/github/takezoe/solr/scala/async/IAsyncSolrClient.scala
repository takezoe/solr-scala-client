package com.github.takezoe.solr.scala.async

import com.github.takezoe.solr.scala.CaseClassMapper
import com.github.takezoe.solr.scala.query.{ExpressionParser, QueryTemplate}
import org.apache.solr.client.solrj.request.UpdateRequest
import org.apache.solr.common.SolrInputDocument

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait IAsyncSolrClient {
    protected implicit def parser: ExpressionParser
    /**
     * Execute given operation in the transaction.
     *
     * The transaction is committed if operation was successful.
     * But the transaction is rolled back if an error occurred.
     */
    def withTransaction[T](operations: => Future[T]): Future[T] = {
        import scala.concurrent.ExecutionContext.Implicits.global

        val p = Promise[T]()
        operations onComplete {
            case Success(x) => commit() onComplete {
                case Success(_) => p success x
                case Failure(t) => p failure t
            }

            case Failure(t) => rollback() onComplete (_ => p failure t)
        }

        p.future
    }

    protected def execute(req: UpdateRequest, promise: Promise[Unit]): Future[Unit]

    def query(query: String): AbstractAsyncQueryBuilder

    /**
     * Add the document.
     *
     * @param doc the document to register
     */
    def add(doc: Any): Future[Unit] = {
        val solrDoc = doc match {
            case sid: SolrInputDocument => sid
            case _ =>
                val ret = new SolrInputDocument
                CaseClassMapper.toMap(doc) map {
                    case (key, value) => ret.addField(key, value)
                }
                ret
        }

        val req = new UpdateRequest()
        req.add(solrDoc)
        execute(req, Promise[Unit]())
    }

    /**
     * Add the document and commit them immediately.
     *
     * @param doc the document to register
     */
    def register(doc: Any): Future[Unit] = {
        withTransaction {
            add(doc)
        }
    }

    /**
     * Delete the document which has a given id.
     *
     * @param id the identifier of the document to delete
     */
    def deleteById(id: String): Future[Unit] = {
        val req = new UpdateRequest()
        req.deleteById(id)
        execute(req, Promise[Unit]())
    }


    /**
     * Delete documents by the given query.
     *
     * @param query the solr query to select documents which would be deleted
     * @param params the parameter map which would be given to the query
     */
    def deleteByQuery(query: String, params: Map[String, Any] = Map()): Future[Unit] = {
        val req = new UpdateRequest()
        req.deleteByQuery(new QueryTemplate(query).merge(params))
        execute(req, Promise[Unit]())
    }


    def commit(): Future[Unit]

    def rollback(): Future[Unit]

    def shutdown(): Unit
}
