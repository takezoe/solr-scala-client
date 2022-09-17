package com.github.takezoe.solr.scala.query

import org.scalatest.funsuite.AnyFunSuite

import com.github.takezoe.solr.scala.TestUtils._

class QueryTemplateSuite extends AnyFunSuite {

  test("Test for parameter replacement".withScalaVersion){
    implicit val parser = new DefaultExpressionParser()
    val query = new QueryTemplate("$KEY$: %VALUE%").merge(Map("KEY" -> "title", "VALUE" -> "(Scala in Action)"))
    assert(query == """title: "\(Scala in Action\)"""")
  }

}