package jp.sf.amateras.solr.scala.sample

import jp.sf.amateras.solr.scala.async._
import jp.sf.amateras.solr.scala.Order

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object AsyncSolrClientSample extends App {

  val client = new AsyncSolrClient("http://localhost:8983/solr")

  val future = client.query("name:%name%")
        .fields("id", "manu", "name")
        .facetFields("manu")
        .sortBy("id", Order.asc)
        .getResultAsMap(Map("name" -> "ThinkPad -X201s"))
            
  future.onComplete {
    case Success(result) => {
      println("count: " + result.numFound)
      result.documents.foreach { doc =>
        println("id: " + doc("id"))
        println("  manu: " + doc.get("manu").getOrElse("<NULL>"))
        println("  name: " + doc("name"))
      }
    }
    case Failure(t) => t.printStackTrace()
  }

}