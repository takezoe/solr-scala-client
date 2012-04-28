package jp.sf.amateras.solr.scala

import org.apache.solr.client.solrj.SolrQuery.ORDER
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer

class SolrClient(url: String) {

  val server = new CommonsHttpSolrServer(url)

  def query(query: String): QueryBuilder = new QueryBuilder(server, query)

  def add(docs: Map[String, Any]*): BatchRegister = new BatchRegister(server, docs: _*)

  def register(docs: Map[String, Any]*): Unit = new BatchRegister(server, docs: _*).commit

  def deleteById(id: String): Unit = server.deleteById(id)

  def deleteByQuery(query: String): Unit = server.deleteByQuery(query)

}

object Test extends App {

  val client = new SolrClient("http://localhost:8983/solr")

  // register
  client
    .add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
    .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X202"))
    .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X100e"))
  .commit

  // query
  val result = client.query("*:*")
        .fields("id", "manu", "name")
        .groupBy("manu")
        .sortBy("id", ORDER.asc)
        .getResult

  result.foreach { doc =>
    println("id: " + doc("id"))
    println("  manu: " + doc("manu"))
    println("  name: " + doc("name"))
  }

}