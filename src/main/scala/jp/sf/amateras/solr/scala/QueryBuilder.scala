package jp.sf.amateras.solr.scala

import scala.collection.JavaConverters._
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.common.SolrDocumentList

class QueryBuilder(server: SolrServer, query: String) {

  val solrQuery = new SolrQuery()

  def fields(fields: String*): QueryBuilder = {
    fields.foreach { field =>
      solrQuery.addField(field)
    }
    this
  }

  def sortBy(field: String, order: SolrQuery.ORDER): QueryBuilder = {
    solrQuery.addSortField(field, order)
    this
  }

  def groupBy(fields: String*): QueryBuilder = {
    solrQuery.setParam("group", "true")
    solrQuery.setParam("group.field", fields: _*)
    this
  }

  def getResult(params: Map[String, Any] = Map()): List[Map[String, Any]] = {

    def toList(docList: SolrDocumentList): List[Map[String, Any]] = {
      (for(i <- 0 to docList.size() - 1) yield {
        val doc = docList.get(i)
        doc.getFieldNames().asScala.map { key =>
          (key, doc.getFieldValue(key))
        }.toMap
      }).toList
    }

    solrQuery.setQuery(new QueryTemplate(query).merge(params))

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