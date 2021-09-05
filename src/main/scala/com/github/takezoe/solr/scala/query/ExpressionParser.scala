package com.github.takezoe.solr.scala.query

import scala.util.parsing.combinator.RegexParsers

trait ExpressionParser {
  def parse(expression: String): AST
}

/**
 * The default implementation of ExpressionParser which supports the simple expression
 * contains operators such as &, | and !.
 */
class DefaultExpressionParser extends RegexParsers with ExpressionParser {

  def operator: Parser[AST] = chainl1(expression,
      "&" ^^ { op => (left: AST, right: AST) => ASTAnd(left, right)} |
      "|" ^^ { op => (left: AST, right: AST) => ASTOr(left, right)} |
      ""  ^^ { op => (left: AST, right: AST) => ASTAnd(left, right)}
  )

  def expression: Parser[AST] = not | phrase | word | "(" ~> operator <~ ")"

  def not: Parser[AST] = "!" ~> expression ^^ ASTNot.apply

  def word: Parser[AST] = """[^(|!& \t)]+""".r ^^ ASTWord.apply

  def phrase: Parser[AST] = "\"" ~> """[^"]+""".r <~ "\"" ^^ ASTPhrase.apply

  def parse(str:String) = parseAll(expression, str).get

}

/**
 * The optional implementation of ExpressionParser which supports Google-like query.
 */
class GoogleExpressionParser extends RegexParsers with ExpressionParser {

  def operator: Parser[AST] = chainl1(expression,
      "OR" ^^ { op => (left: AST, right: AST) => ASTOr(left, right)} |
      ""   ^^ { op => (left: AST, right: AST) => ASTAnd(left, right)}
  )

  def expression: Parser[AST] = not | phrase | word | "(" ~> operator <~ ")"

  def not: Parser[AST] = "-" ~> expression ^^ ASTNot.apply

  def word: Parser[AST] = """[^(\- \t)]+""".r ^^ ASTWord.apply

  def phrase: Parser[AST] = "\"" ~> """[^"]+""".r <~ "\"" ^^ ASTPhrase.apply

  def parse(str:String) = parseAll(expression, str).get

}

class ExpressionParseException(cause: Throwable) extends RuntimeException(cause)

object ExpressionParser {

  /**
   * Parses the given expression and returns the Solr query.
   *
   * @param str    the expression
   * @param parser the expression parser (default is [[DefaultExpressionParser]])
   * @return the Solr query
   * @throws ExpressionParseException Failed to parse the given expression.
   */
  def parse(str: String)(implicit parser: ExpressionParser): String = {
    try {
      "(" + visit(parser.parse("(" + normalize(str) + ")")) + ")"
    } catch {
      case e : Throwable => throw new ExpressionParseException(e)
    }
  }

  /**
   * Converts the full-width space to the half-wide space.
   */
  private def normalize(str: String): String = str.replaceAll("　", " ")

  /**
   * Visits nodes of the given AST recursive and assembles Solr query.
   */
  private def visit(ast: AST): String = {
    ast match {
      case and    : ASTAnd    => "(" + visit(and.left) + " AND " + visit(and.right) + ")"
      case or     : ASTOr     => "(" + visit(or.left) + " OR " + visit(or.right) + ")"
      case not    : ASTNot    => "NOT (" + visit(not.expr) + ")"
      case phrase : ASTPhrase => "\"" + QueryUtils.escape(phrase.value) + "\""
      case word   : ASTWord   => QueryUtils.escape(word.value)
    }
  }

}