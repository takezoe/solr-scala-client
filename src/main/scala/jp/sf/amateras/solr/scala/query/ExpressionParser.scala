package jp.sf.amateras.solr.scala.query;
import scala.util.parsing.combinator.RegexParsers

/**
 * Parses an expression and assembles Solr query.
 */
class ExpressionParser extends RegexParsers {
  
  def operator: Parser[AST] = chainl1(expression, 
      "&"^^{ op => (left, right) => ASTAnd(left, right)}|
      "|"^^{ op => (left, right) => ASTOr(left, right)}|
      ""^^{ op => (left, right) => ASTAnd(left, right)}
  )
    
  def expression: Parser[AST] = not|literal|word|"("~>operator<~")"
  
  def not: Parser[AST] = "!"~>expression^^ASTNot
  
  def word: Parser[AST] = """[^(%|!& \t)]+""".r^^ASTWord

  def literal: Parser[AST] = "\""~>"""[^"]+""".r<~"\""^^ASTWord
  
  def parse(str:String) = parseAll(expression, str)
  
}

class ExpressionParseException(cause: Throwable) extends RuntimeException(cause)

object ExpressionParser extends App {
  
  def parse(str: String): String = {
    try {
      visit(new ExpressionParser().parse("(" + str + ")").get)
    } catch {
      case e => throw new ExpressionParseException(e)
    }
  }
  
  def visit(ast: AST): String = {
    ast match {
      case and : ASTAnd  => "(" + visit(and.left) + " AND " + visit(and.right) + ")"
      case or  : ASTOr   => "(" + visit(or.left) + " OR " + visit(or.right) + ")"
      case not : ASTNot  => "NOT " + visit(not.expr)
      case word: ASTWord => "\"" + QueryUtils.escape(word.value) + "\""
    }
  }
  
  println(ExpressionParser.parse("ThinkPad !X201s"))
}