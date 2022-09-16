package com.github.takezoe.solr.scala

import org.testcontainers.containers.SolrContainer
import java.{util => ju}
import org.scalatest.funsuite.AnyFunSuite

import com.github.takezoe.solr.scala.TestUtils._

class SolrIntegrationTest extends AnyFunSuite {

  test("Integration test".withScalaVersion){
    val container = new SolrContainer()
        .withCollection("pc")
        .withConfiguration("pc", getClass().getResource("/solrconfig.xml"))
        .withSchema(getClass().getResource("/managed-schema"))
    
    container.start()
    try {
      val client = new SolrClient(s"http://${container.getContainerIpAddress()}:${container.getSolrPort()}/solr/pc")

      // register as Map
      client
        .add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
        .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X220"))
        .add(Map("id"->"003", "manu" -> "Dell", "name" -> "XPS 13"))
        .commit()

      // query as Map
      val result1 = client.query("name: %name%")
        .fields("id", "manu", "name")
        .sortBy("id", Order.asc)
        .getResultAsMap(Map("name" -> "ThinkPad"))

      val expect1 = List(
        Map("id" -> "001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"),
        Map("id" -> "002", "manu" -> "Lenovo", "name" -> "ThinkPad X220")
      )

      // verify
      assert(expect1 == result1.documents)

      // register as case class
      client
        .add(PC("004", "Apple", "MacBook Pro"))
        .commit()

      // query as case class
      val result2 = client.query("manu: %manu%")
        .fields("id", "manu", "name")
        .sortBy("id", Order.asc)
        .getResultAs[PC](Map("manu" -> "Apple"))

      val expect2 = List(
        PC("004", "Apple", "MacBook Pro")
      )

      // verify
      assert(expect2 == result2.documents)

      // register as case class
      client
        .add(PC("005", "Microsoft", "Surface Pro 7"))
        .commit()

      // query with new sort
      val result3 = client.query("*:*")
        .fields("id", "manu", "name")
        .sortBy(
          ("manu", Order.asc),
          ("id", Order.asc)
        )
        .getResultAs[PC](null)

      val expect3 = List(
        PC("004", "Apple", "MacBook Pro"),
        PC("003", "Dell", "XPS 13"),
        PC("001", "Lenovo", "ThinkPad X201s"),
        PC("002", "Lenovo", "ThinkPad X220"),
        PC("005", "Microsoft", "Surface Pro 7")
      )

      assert(expect3 == result3.documents)
    } finally {
      container.stop()
    }
  }

}

case class PC(id: String, manu: String, name: String)

