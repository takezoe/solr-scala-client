package jp.sf.amateras.solr.scala.sample

import jp.sf.amateras.solr.scala.async._
import jp.sf.amateras.solr.scala.Order

object AsyncSolrClientSample extends App {

  val client = new AsyncSolrClient("http://localhost:8983/solr")

  client.query("name:%name%")
        .fields("id", "manu", "name")
        .facetFields("manu")
        .sortBy("id", Order.asc)
        .getResultAsMap(Map("name" -> "ThinkPad -X201s"), { result =>
    result.documents.foreach { doc =>
      println("id: " + doc("id"))
      println("  manu: " + doc.get("manu").getOrElse("<NULL>"))
      println("  name: " + doc("name"))
    }
  })
  
  //client.deleteById("001")

}