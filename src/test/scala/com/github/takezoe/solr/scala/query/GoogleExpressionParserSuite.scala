package com.github.takezoe.solr.scala.query

import org.scalatest.FunSuite

class GoogleExpressionParserSuite extends FunSuite {

  test("Test for implicit AND chain"){
    val result = new GoogleExpressionParser().parse("(AA BB CC)")
    assert(result == ASTAnd(ASTAnd(ASTWord("AA"), ASTWord("BB")), ASTWord("CC")))
  }

  test("Test for implicit AND, OR and NOT combination"){
    val result = new GoogleExpressionParser().parse("(AA BB OR CC -DD)")
    assert(result == ASTAnd(ASTOr(ASTAnd(ASTWord("AA"), ASTWord("BB")), ASTWord("CC")), ASTNot(ASTWord("DD"))))
  }
  
  test("Test for quatation"){
    val result = new GoogleExpressionParser().parse("(\"AA & BB\" OR(\"(CC)\" - DD))")
    assert(result == ASTOr(ASTPhrase("AA & BB"), ASTAnd(ASTPhrase("(CC)"), ASTNot(ASTWord("DD")))))
  }
  
}
