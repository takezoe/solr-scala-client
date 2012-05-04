package jp.sf.amateras.solr.scala

import org.apache.solr.client.solrj.SolrQuery.ORDER
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer

/**
 * This is the simple Apache Solr client for Scala.
 */
class SolrClient(url: String) {

  val server = new CommonsHttpSolrServer(url)

  /**
   * Search documents using the given query.
   *
   * {{{
   * import jp.sf.amateras.solr.scala._
   *
   * val client = new SolrClient("http://localhost:8983/solr")
   *
   * val result: List[Map[String, Any]] =
   *   client.query("*:*")
   *         .fields("id", "manu", "name")
   *         .sortBy("id", Order.asc)
   *         .getResult()
   * }}}
   */
  def query(query: String): QueryBuilder = new QueryBuilder(server, query)

  /**
   * Execute batch updating.
   *
   * Note: To register documents actual, you have to call commit after added them.
   *
   * {{{
   * import jp.sf.amateras.solr.scala._
   *
   * val client = new SolrClient("http://localhost:8983/solr")
   *
   * client.add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
   *       .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X202"))
   *       .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X100e"))
   *       .commit
   * }}}
   */
  def add(docs: Map[String, Any]*): BatchRegister = new BatchRegister(server, docs: _*)

  /**
   * Add documents and commit them immediatly.
   */
  def register(docs: Map[String, Any]*): Unit = new BatchRegister(server, docs: _*).commit

  /**
   * Delete the document which has a given id.
   */
  def deleteById(id: String): Unit = server.deleteById(id)

  /**
   * Delete documents by the given query.
   */
  def deleteByQuery(query: String, params: Map[String, Any] = Map()): Unit = {
    server.deleteByQuery(new QueryTemplate(query).merge(params))
  }

}
