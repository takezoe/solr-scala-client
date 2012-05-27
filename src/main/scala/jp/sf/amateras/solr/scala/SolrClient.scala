package jp.sf.amateras.solr.scala

import org.apache.solr.client.solrj.SolrQuery.ORDER
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * This is the simple Apache Solr client for Scala.
 */
class SolrClient(url: String, initializer: (CommonsHttpSolrServer) => Unit = { server => Unit }) {

  private val server = new CommonsHttpSolrServer(url)
  initializer(server)

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
   *         .getResultAsMap()
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
   *
   * @param docs documents to register
   */
  def register(docs: Map[String, Any]*): Unit = new BatchRegister(server, docs: _*).commit

  /**
   * Delete the document which has a given id.
   *
   * @param id the identifier of the document to delete
   */
  def deleteById(id: String): Unit = server.deleteById(id)

  /**
   * Delete documents by the given query.
   *
   * @param query the solr query to select documents which would be deleted
   * @param params the parameter map which would be given to the query
   */
  def deleteByQuery(query: String, params: Map[String, Any] = Map()): Unit = {
    server.deleteByQuery(new QueryTemplate(query).merge(params))
  }

}
