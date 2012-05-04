package jp.sf.amateras.solr.scala.sample

import _root_.jp.sf.amateras.solr.scala._

class SolrClientSample extends App {

  val client = new SolrClient("http://localhost:8983/solr")

  // register
  client
    .add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
    .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X202"))
    .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X100e"))
  .commit

  // query
  val result = client.query("name:%name%")
        .fields("id", "manu", "name")
        .sortBy("id", Order.asc)
        .getResult(Map("name" -> "ThinkPad X201s"))

  result.foreach { doc =>
    println("id: " + doc("id"))
    println("  manu: " + doc("manu"))
    println("  name: " + doc("name"))
  }

}