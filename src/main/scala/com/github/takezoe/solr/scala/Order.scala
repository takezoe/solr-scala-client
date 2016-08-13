package com.github.takezoe.solr.scala

import org.apache.solr.client.solrj.SolrQuery.ORDER

import scala.language.implicitConversions

sealed trait Order {
  val asEnum: ORDER
}

object Order {

  object asc extends Order {
    override lazy val asEnum = ORDER.asc
  }

  object desc extends Order {
    override lazy val asEnum = ORDER.desc
  }

  implicit def orderConverter(order: Order): ORDER = order.asEnum

}