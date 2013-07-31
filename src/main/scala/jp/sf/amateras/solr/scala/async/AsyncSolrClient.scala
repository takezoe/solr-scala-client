package jp.sf.amateras.solr.scala.async

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import AsyncUtils._
import jp.sf.amateras.solr.scala.query._

class AsyncSolrClient(url: String)
    (implicit factory: (String) => AsyncSolrServer = { (url: String) => new AsyncSolrServer(url) }, 
            parser: ExpressionParser = new DefaultExpressionParser()) {
  
  val server = factory(url)
  
  def withTransaction[T](operations: (AsyncSolrClient) => Future[T]): Future[T] = {
    operations(this) map { result =>
      server.commit()
      result
    }
  }
  
  def query(query: String): AsyncQueryBuilder = new AsyncQueryBuilder(server, query)
  
  def deleteById(id: String): Future[Unit] = server.deleteById(id)
  
  def commit(): Future[Unit] = server.commit()

  def shutdown(): Unit = server.shutdown()
  
}