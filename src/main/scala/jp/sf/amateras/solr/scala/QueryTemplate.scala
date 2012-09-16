package jp.sf.amateras.solr.scala

/**
 * Provides templating for the solr query.
 * This class makes place holder and simple string replacement.
 *
 * You can write variables in the query as the following:
 *
 *  - %VAR_NAME% : place holder (parameter would be escaped)
 *  - \$VAR_NAME\$ : string replacement (parameter would be not escaped)
 */
private[scala] class QueryTemplate(query: String) {

  /**
   * Merges given parameters to this template and returns the result.
   *
   * @param params the parameter map which would be merged with this template
   * @return the merged string
   */
  def merge(params: Map[String, Any]): String = {
    var result = query
    params.foreach { case (key, value) =>
      result = result.replaceAll("%" + key + "%", "\"" + QueryTemplate.escape(value.toString) + "\"")
    }
    params.foreach { case (key, value) =>
      result = result.replaceAll("\\$" + key + "\\$", value.toString)
    }
    result
  }

}

private[scala] object QueryTemplate {

  private lazy val specialCharacters =
    Set('+', '-', '&', '|',  '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\')

  /**
   * Escapes special characters in the solr query.
   *
   * @param value the source string
   * @return the escaped string
   */
  private def escape(value: String): String =
    value.toString.map { c =>
      c match {
        case _ if specialCharacters.contains(c) => Seq('\\', '\\', c)
        case _ => Seq(c)
      }
    }.flatten.mkString

}