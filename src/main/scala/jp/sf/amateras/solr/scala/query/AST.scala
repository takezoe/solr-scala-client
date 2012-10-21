package jp.sf.amateras.solr.scala.query;

trait AST

case class ASTAnd(left: AST, right: AST) extends AST
case class ASTOr(left: AST, right: AST) extends AST
case class ASTNot(expr: AST) extends AST
case class ASTWord(value: String) extends AST
case class ASTPhrase(value: String) extends AST
