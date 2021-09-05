package com.github.takezoe.solr.scala

import com.github.takezoe.solr.scala.query.{DefaultExpressionParser, ExpressionParser, QueryTemplate}
import org.apache.solr.client.solrj.{SolrClient => ApacheSolrClient}
import org.apache.solr.client.solrj.impl.{HttpSolrClient => ApacheHttpSolrClient}
import org.apache.solr.client.solrj.response.{SolrPingResponse, UpdateResponse}

/**
 * This is the simple Apache Solr client for Scala.
 */
class SolrClient(url: String)
  (implicit factory: (String) => ApacheSolrClient = { (url: String) => new ApacheHttpSolrClient.Builder(url).build() },
            parser: ExpressionParser = new DefaultExpressionParser()) {

  private val server = factory(url)
  //initializer(server)

  /**
   * Shutdown this solr client to release allocated resources.
   */
  def shutdown(): Unit = server.close()

  /**
   * Execute given operation in the transaction.
   *
   * The transaction is committed if operation was successful.
   * But the transaction is rolled back if an error occurred.
   */
  def withTransaction[T](operations: => T): T = {
    try {
      val result = operations
      commit()
      result
    } catch {
      case t: Throwable => {
        rollback()
        throw t
      }
    }
  }

  /**
    * Execute given operation in the transaction to a collection
    *
    * The transaction is committed if operation was successful.
    * But the transaction is rolled back if an error occurred.
    */
  def withTransactionOnCollection[T](collection: String)(operations: => T): T = {
    try {
      val result = operations
      commit(collection)
      result
    } catch {
      case t: Throwable => {
        rollback(collection)
        throw t
      }
    }
  }

  /**
    * Check the status of the server
    * You HAVE TO instantiate the client with a collection b/c /solr/admin/ping does not exist.
    *
    * {{{
    *   val client = new SolrClient("http://localhost:8983/solr/collection")
    *   val result: SolrPingResponse = client.ping
    * }}}
    */
  def ping: SolrPingResponse = server.ping()

  /**
   * Search documents using the given query, note the difference in instantiation as well as use of method collection
   *
   * {{{
   * import jp.sf.amateras.solr.scala._
   *
   * val client = new SolrClient("http://localhost:8983/solr")
   *
   * val result: List[Map[String, Any]] =
   *   client.query("*:*")
   *        .collection("collection") <-- Required or you'll be searching /solr/select
   *         .fields("id", "manu", "name")
   *         .sortBy("id", Order.asc)
   *         .getResultAsMap()
   *
   * --- OR ---
   * val client = new SolrClient("http://localhost:8983/solr/collection")
   * val result: List[Map[String, Any]] =
   *   client.query("*:*")
   *         .fields("id", "manu", "name")
   *         .sortBy("id", Order.asc)
   *         .getResultAsMap()
   * }}}
   */
  def query(query: String): QueryBuilder = new QueryBuilder(server, query)

  /**
   * Execute batch updating.
   *
   * Note --- VERY IMPORTANT ---: You MUST supply a collection
   * when instantiating the client or you will see an NPE
   *
   * Note: To register documents actual, you have to call commit after added them.
   *
   * {{{
   * import jp.sf.amateras.solr.scala._
   *
   * val client = new SolrClient("http://localhost:8983/solr/collection") <-- collection included in URI
   *
   * client.add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
   *       .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X202"))
   *       .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X100e"))
   *       .commit
   * }}}
   */
  def add(docs: Any*): BatchRegister = new BatchRegister(server, None, CaseClassMapper.toMapArray(docs: _*).toIndexedSeq: _*)

  /**
   * Execute batch updating on the specified collection.
   *
   * Note: To register documents actual, you have to call commit after added them.
   *
   * {{{
   * import jp.sf.amateras.solr.scala._
   *
   * val client = new SolrClient("http://localhost:8983/solr") <-- No collection at end of URI
   *
   * client.addToCollection("collection", Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
   *       .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X202"))
   *       .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X100e"))
   *       .commit
   * }}}
   */
  def addToCollection(collection: String, docs: Any*): BatchRegister = new BatchRegister(server, Some(collection), CaseClassMapper.toMapArray(docs: _*).toIndexedSeq: _*)

  /**
   * Add documents and commit them immediately.
   * See note about client instantiation in `add` documentation
   * @param docs documents to register
   */
  def register(docs: Any*): UpdateResponse = new BatchRegister(server, None, CaseClassMapper.toMapArray(docs: _*).toIndexedSeq: _*).commit()

  /**
   * Add documents and commit them immediately to the specified collection.
   *
   * @param collection the name of the collection
   * @param docs documents to register
   */
  def registerToCollection(collection: String, docs: Any*): UpdateResponse = new BatchRegister(server, Some(collection), CaseClassMapper.toMapArray(docs: _*).toIndexedSeq: _*).commit(collection)

  /**
   * Delete the document which has a given id.
   * See note about client instantiation in `add` documentation
   *
   * @param id the identifier of the document to delete
   */
  def deleteById(id: String): UpdateResponse = server.deleteById(id)

  /**
   * Delete the document which has a given id in the specified collection.
   *
   * @param collection the name of the collection
   * @param id the identifier of the document to delete
   */
  def deleteById(collection: String, id: String): UpdateResponse = server.deleteById(collection, id)

  /**
   * Delete documents by the given query.
   * See note about client instantiation in `add` documentation
   *
   * @param query the solr query to select documents which would be deleted
   * @param params the parameter map which would be given to the query
   */
  def deleteByQuery(query: String, params: Map[String, Any] = Map()): UpdateResponse = {
    server.deleteByQuery(new QueryTemplate(query).merge(params))
  }

  /**
   * Delete documents by the given query on the specified collection.
   *
   * @param collection the name of the collection
   * @param query the solr query to select documents which would be deleted
   * @param params the parameter map which would be given to the query
   */
  def deleteByQueryForCollection(collection: String, query: String, params: Map[String, Any] = Map()): UpdateResponse = {
    server.deleteByQuery(collection, new QueryTemplate(query).merge(params))
  }

  /**
    * Commit the current session on a specified collection
    */
  def commit(collection: String): UpdateResponse = server.commit(collection)
  /**
   * Commit the current session.
   */
  def commit(): UpdateResponse = server.commit


  /**
    * Rolled back the current session on a collection
    */
  def rollback(collection: String): UpdateResponse = server.rollback(collection)

  /**
   * Rolled back the current session.
   */
  def rollback(): UpdateResponse = server.rollback

}
