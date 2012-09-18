package jp.sf.amateras.solr.scala.query
import org.scalatest.FunSuite

class QueryTemplateSuite extends FunSuite {

  test("Test for parameter replacement"){
    implicit val parser = new DefaultExpressionParser()
	val query = new QueryTemplate("$KEY$: %VALUE%").merge(Map("KEY" -> "title", "VALUE" -> "(Scala in Action)"))
	assert(query == """title: "\(Scala in Action\)"""")
  }

}