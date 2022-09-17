package com.github.takezoe.solr.scala

import org.apache.solr.client.solrj.{SolrClient => ApacheSolrClient}
import org.apache.solr.common.SolrInputDocument
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers

import org.scalatest.funsuite.AnyFunSuite

import com.github.takezoe.solr.scala.TestUtils._

class BatchRegisterSuite extends AnyFunSuite {

  test("commit calls SolrServer#commit.".withScalaVersion){
    val solr = mock(classOf[ApacheSolrClient])
    val register = new BatchRegister(solr, None)
    register.commit()

    verify(solr, times(1)).commit()
  }

  test("rollback calls SolrServer#commit.".withScalaVersion){
    val solr = mock(classOf[ApacheSolrClient])
    val register = new BatchRegister(solr, None)
    register.rollback()

    verify(solr, times(1)).rollback()
  }

  test("add a document via the constructor.".withScalaVersion){
    val solr = mock(classOf[ApacheSolrClient])
    val register = new BatchRegister(solr, None, Map("id" -> "123"))

    val captor: ArgumentCaptor[SolrInputDocument] = ArgumentCaptor.forClass(classOf[SolrInputDocument])
    verify(solr, times(1)).add(captor.capture())

    val doc = captor.getValue()
    assert(doc.getField("id").getValue() == "123")
  }

  test("add a document via the constructor with collection.".withScalaVersion){
    val solr = mock(classOf[ApacheSolrClient])
    val register = new BatchRegister(solr, Some("collection"), Map("id" -> "123"))

    val captor: ArgumentCaptor[SolrInputDocument] = ArgumentCaptor.forClass(classOf[SolrInputDocument])
    verify(solr, times(1)).add(ArgumentMatchers.eq("collection"), captor.capture())

    val doc = captor.getValue()
    assert(doc.getField("id").getValue() == "123")
  }

  test("add documents via the constructor.".withScalaVersion){
    val solr = mock(classOf[ApacheSolrClient])
    val register = new BatchRegister(solr, None, Map("id" -> "123"), Map("id" -> "456"))

    val captor: ArgumentCaptor[SolrInputDocument] = ArgumentCaptor.forClass(classOf[SolrInputDocument])
    verify(solr, times(2)).add(captor.capture())

    val doc1 = captor.getAllValues().get(0)
    assert(doc1.getField("id").getValue() == "123")

    val doc2 = captor.getAllValues().get(1)
    assert(doc2.getField("id").getValue() == "456")
  }

  test("add documents via the constructor with collection.".withScalaVersion){
    val solr = mock(classOf[ApacheSolrClient])
    val register = new BatchRegister(solr, Some("collection"), Map("id" -> "123"), Map("id" -> "456"))

    val captor: ArgumentCaptor[SolrInputDocument] = ArgumentCaptor.forClass(classOf[SolrInputDocument])
    verify(solr, times(2)).add(ArgumentMatchers.eq("collection"), captor.capture())

    val doc1 = captor.getAllValues().get(0)
    assert(doc1.getField("id").getValue() == "123")

    val doc2 = captor.getAllValues().get(1)
    assert(doc2.getField("id").getValue() == "456")
  }
}
