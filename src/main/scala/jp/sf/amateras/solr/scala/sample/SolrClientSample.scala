package jp.sf.amateras.solr.scala.sample

import _root_.jp.sf.amateras.solr.scala._

object SolrClientSample extends App {

  val client = new SolrClient("http://localhost:8983/solr")

  // register
  client
    .add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
    .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X202"))
    .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X100e"))
  .commit

  // query (Map)
  val result1 = client.query("name:%name%")
        .fields("id", "manu", "name")
        .facetFields("manu")
        .sortBy("id", Order.asc)
        .getResultAsMap(Map("name" -> "ThinkPad X201s"))

  println("-- matched documents --")
  result1.documents.foreach { doc =>
    println("id: " + doc("id"))
    println("  manu: " + doc("manu"))
    println("  name: " + doc("name"))
  }

  println("-- facet counts --")
  result1.facetFields.foreach { case (field, counts) =>
    println("field: " + field)
    counts.foreach { case (manu, count) =>
      println("  " + manu + ": " + count)
    }
  }

  // query (case class)
  println("-- case class --")
  val result2 = client.query("name:%name%")
        .fields("id", "manu", "name")
        .facetFields("manu")
        .sortBy("id", Order.asc)
        .getResultAs[Product](Map("name" -> "ThinkPad X201s"))

  result2.documents.foreach { product =>
    println(product)
  }

}

case class Product(id: String, manu: String, name: String)
