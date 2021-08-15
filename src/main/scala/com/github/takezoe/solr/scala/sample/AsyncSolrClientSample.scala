package com.github.takezoe.solr.scala.sample

import com.github.takezoe.solr.scala.Order
import com.github.takezoe.solr.scala.async.AsyncSolrClient

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object AsyncSolrClientSample extends App {

  val client = new AsyncSolrClient("http://localhost:8983/solr")

  val f1 = client.register(Map("id" -> "005", "name" -> "ThinkPad X1 Carbon", "manu" -> "Lenovo"))
  
  val f2 = client.withTransaction {
    for {
        _ <- client.add(Map("id" -> "006", "name" -> "Nexus7 2012", "manu" -> "ASUS"))
        _ <- client.add(Map("id" -> "007", "name" -> "Nexus7 2013", "manu" -> "ASUS"))
    } yield ()
  }
  
  val f3 = client.query("name:%name%")
        .fields("id", "manu", "name")
        .facetFields("manu")
        .sortBy("id", Order.asc)
        .getResultAsMap(Map("name" -> "ThinkPad X201s"))
            
  f3.onComplete {
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

  val future = for {
    _ <- Future.sequence(List(f1, f2))
    _ <- f3
  } yield ()

  Await.result(future, Duration.Inf)
  client.shutdown()
}