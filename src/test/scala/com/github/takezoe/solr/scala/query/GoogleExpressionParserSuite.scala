package com.github.takezoe.solr.scala.query

import org.scalatest.funsuite.AnyFunSuite

import com.github.takezoe.solr.scala.TestUtils._

class GoogleExpressionParserSuite extends AnyFunSuite {

  test("Test for implicit AND chain".withScalaVersion){
    val result = new GoogleExpressionParser().parse("(AA BB CC)")
    assert(result == ASTAnd(ASTAnd(ASTWord("AA"), ASTWord("BB")), ASTWord("CC")))
  }

  test("Test for implicit AND, OR and NOT combination".withScalaVersion){
    val result = new GoogleExpressionParser().parse("(AA BB OR CC -DD)")
    assert(result == ASTAnd(ASTOr(ASTAnd(ASTWord("AA"), ASTWord("BB")), ASTWord("CC")), ASTNot(ASTWord("DD"))))
  }

  test("Test for quatation".withScalaVersion){
    val result = new GoogleExpressionParser().parse("(\"AA & BB\" OR(\"(CC)\" - DD))")
    assert(result == ASTOr(ASTPhrase("AA & BB"), ASTAnd(ASTPhrase("(CC)"), ASTNot(ASTWord("DD")))))
  }
}
