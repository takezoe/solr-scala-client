package com.github.takezoe.solr.scala

import org.apache.solr.client.solrj.{SolrClient => ApacheSolrClient}
import org.apache.solr.common.SolrInputDocument
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.ArgumentMatchers

class BatchRegisterSuite extends FunSuite with MockitoSugar {

  test("commit calls SolrServer#commit."){
    val solr = mock[ApacheSolrClient]
    val register = new BatchRegister(solr, null)
    register.commit()

    verify(solr, times(1)).commit()
  }

  test("rollback calls SolrServer#commit."){
    val solr = mock[ApacheSolrClient]
    val register = new BatchRegister(solr, null)
    register.rollback()

    verify(solr, times(1)).rollback()
  }

  test("add a document via the constructor."){
    val solr = mock[ApacheSolrClient]
    val register = new BatchRegister(solr, null, Map("id" -> "123"))

    val captor = ArgumentCaptor.forClass(classOf[SolrInputDocument])
    verify(solr, times(1)).add(ArgumentMatchers.eq[String](null), captor.capture())

    val doc = captor.getValue()
    assert(doc.getField("id").getValue() == "123")
  }

  test("add documents via the constructor."){
    val solr = mock[ApacheSolrClient]
    val register = new BatchRegister(solr, null, Map("id" -> "123"), Map("id" -> "456"))

    val captor = ArgumentCaptor.forClass(classOf[SolrInputDocument])
    verify(solr, times(2)).add(ArgumentMatchers.eq[String](null), captor.capture())

    val doc1 = captor.getAllValues().get(0)
    assert(doc1.getField("id").getValue() == "123")

    val doc2 = captor.getAllValues().get(1)
    assert(doc2.getField("id").getValue() == "456")
  }
}
