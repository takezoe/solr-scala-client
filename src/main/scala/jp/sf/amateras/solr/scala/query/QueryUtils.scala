package jp.sf.amateras.solr.scala.query;

object QueryUtils {
  
  private lazy val specialCharacters =
    Set('+', '-', '&', '|',  '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':')

  /**
   * Escapes special characters in the solr query.
   *
   * @param value the source string
   * @return the escaped string
   */
  def escape(value: String): String =
    value.toString.map { c =>
      c match {
        case '\\' => Seq('\\', '\\')
        case _ if specialCharacters.contains(c) => Seq('\\', c)
        case _ => Seq(c)
      }
    }.flatten.mkString
    
}
