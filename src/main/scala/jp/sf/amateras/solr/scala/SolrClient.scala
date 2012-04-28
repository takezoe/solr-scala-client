package jp.sf.amateras.solr.scala

import scala.collection.JavaConverters._
import org.apache.solr.client.solrj._
import org.apache.solr.client.solrj.impl._
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument

class SolrClient(url: String) {

  val server = new CommonsHttpSolrServer(url)

  def query(query: String): AutoQuery = new AutoQuery(server, query)

  def add(docs: Map[String, Any]*): AutoRegister = new AutoRegister(server, docs: _*)

  def register(docs: Map[String, Any]*): Unit = new AutoRegister(server, docs: _*).commit

  def deleteById(id: String): Unit = server.deleteById(id)

  def deleteByQuery(query: String): Unit = server.deleteByQuery(query)

}

class AutoRegister(server: SolrServer, docs: Map[String, Any]*) {

  docs.foreach { doc =>
    val solrDoc = new SolrInputDocument
    doc.map { case (key, value) =>
      solrDoc.addField(key, value)
    }
    server.add(solrDoc)
  }

  def add(docs: Map[String, Any]*): AutoRegister = {
    docs.foreach { doc =>
      val solrDoc = new SolrInputDocument
      doc.map { case (key, value) =>
        solrDoc.addField(key, value)
      }
      server.add(solrDoc)
    }
    this
  }

  def commit(): Unit = server.commit

  def rollback(): Unit = server.rollback

}

class AutoQuery(server: SolrServer, query: String) {

  val solrQuery = new SolrQuery(query)

  def fields(fields: String*): AutoQuery = {
    fields.foreach { field =>
      solrQuery.addField(field)
    }
    this
  }

  def sortBy(field: String, order: SolrQuery.ORDER): AutoQuery = {
    solrQuery.addSortField(field, order)
    this
  }

  def groupBy(fields: String*): AutoQuery = {
    solrQuery.setParam("group", "true")
    solrQuery.setParam("group.field", fields: _*)
    this
  }

  def getResult(): List[Map[String, Any]] = {
    def toList(docList: SolrDocumentList): List[Map[String, Any]] = {
      (for(i <- 0 to docList.size() - 1) yield {
        val doc = docList.get(i)
        doc.getFieldNames().asScala.map { key =>
          (key, doc.getFieldValue(key))
        }.toMap
      }).toList
    }

    val response = server.query(solrQuery)
    solrQuery.getParams("group") match {
      case null => {
        toList(response.getResults())
      }
      case _ => {
        val groupResponse = response.getGroupResponse()
        groupResponse.getValues().asScala.map { groupCommand =>
          groupCommand.getValues().asScala.map { group =>
            toList(group.getResult())
          }.flatten
        }.flatten.toList
      }

    }
  }

}

object Test extends App {

  import SolrQuery.ORDER

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