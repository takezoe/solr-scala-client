package jp.sf.amateras.solr.scala
import org.scalatest.FunSuite
import org.apache.solr.client.solrj.SolrServer
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentCaptor
import org.apache.solr.common.SolrInputDocument

class BatchRegisterSuite extends FunSuite with MockitoSugar {

  test("commit calls SolrServer#commit."){
    val server = mock[SolrServer]
    val register = new BatchRegister(server)
    register.commit()

    verify(server, times(1)).commit()
  }

  test("rollback calls SolrServer#commit."){
    val server = mock[SolrServer]
    val register = new BatchRegister(server)
    register.rollback()

    verify(server, times(1)).rollback()
  }

  test("add a document via the constructor."){
    val server = mock[SolrServer]
    val register = new BatchRegister(server, Map("id" -> "123"))

    val captor = ArgumentCaptor.forClass(classOf[SolrInputDocument])
    verify(server, times(1)).add(captor.capture())

    val doc = captor.getValue()
    assert(doc.getField("id").getValue() == "123")
  }

  test("add documents via the constructor."){
    val server = mock[SolrServer]
    val register = new BatchRegister(server,
        Map("id" -> "123"), Map("id" -> "456"))

    val captor = ArgumentCaptor.forClass(classOf[SolrInputDocument])
    verify(server, times(2)).add(captor.capture())

    val doc1 = captor.getAllValues().get(0)
    assert(doc1.getField("id").getValue() == "123")

    val doc2 = captor.getAllValues().get(1)
    assert(doc2.getField("id").getValue() == "456")
  }
}