package jp.sf.amateras.solr.scala.query;
import org.scalatest.FunSuite

class DefaultExpressionParserSuite extends FunSuite {
	
  test("Test for implicit AND chain"){
	val result = new DefaultExpressionParser().parse("(AA BB CC)")
	assert(result == ASTAnd(ASTAnd(ASTWord("AA"), ASTWord("BB")), ASTWord("CC")))
  }

  test("Test for implicit AND, OR and NOT combination"){
	val result = new DefaultExpressionParser().parse("(AA & BB | CC !DD)")
	assert(result == ASTAnd(ASTOr(ASTAnd(ASTWord("AA"), ASTWord("BB")), ASTWord("CC")), ASTNot(ASTWord("DD"))))
  }
  
  test("Test for quatation"){
	val result = new DefaultExpressionParser().parse("(\"AA & BB\" |(\"(CC)\" !DD))")
	assert(result == ASTOr(ASTWord("AA & BB"), ASTAnd(ASTWord("(CC)"), ASTNot(ASTWord("DD")))))
  }
  
}
