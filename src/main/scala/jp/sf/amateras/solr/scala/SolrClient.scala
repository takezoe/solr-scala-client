package jp.sf.amateras.solr.scala

import scala.collection.JavaConverters._
import org.apache.solr.client.solrj._
import org.apache.solr.client.solrj.impl._
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList

class SolrClient(url: String) {

  val server = new CommonsHttpSolrServer(url)

  def query(query: String): AutoQuery = new AutoQuery(server, query)

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

  def getResult(): List[SolrDocument] = {
    def toList(docList: SolrDocumentList): List[SolrDocument] = {
      (for(i <- 0 to docList.size() - 1) yield {
        docList.get(i)
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

//object Test extends App {
//
//  import SolrQuery.ORDER
//
//  val client = new SolrClient("http://localhost:8983/solr")
//  client.query("*:*")
//        .fields("id", "manu", "name")
//        .groupBy("manu")
//        .sortBy("id", ORDER.asc)
//        .getResult.foreach { doc =>
//    println(doc.getFieldValue("id"))
//    println(doc.getFieldValue("manu"))
//    println(doc.getFieldValue("name"))
//    println("--")
//  }
//
//}