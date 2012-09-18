package jp.sf.amateras.solr.scala.query

/**
 * Provides templating for the solr query.
 * This class makes place holder and simple string replacement.
 *
 * You can write variables in the query as the following:
 *
 *  - %VAR_NAME% : place holder to set a single word (parameter would be escaped)
 *  - ?VAR_NAME? : place holder to set an expression (&, | and ! are available in an expression)
 *  - \$VAR_NAME\$ : string replacement (parameter would be not escaped)
 */
class QueryTemplate(query: String) {

  /**
   * Merges given parameters to this template and returns the result.
   *
   * @param params the parameter map which would be merged with this template
   * @return the merged string
   */
  def merge(params: Map[String, Any])(implicit parser: ExpressionParser): String = {
    var result = query
    params.foreach { case (key, value) =>
      result = result.replaceAll("\\?" + key + "\\?", ExpressionParser.parse(value.toString))
    }
    params.foreach { case (key, value) =>
      result = result.replaceAll("%" + key + "%", "\"" + QueryUtils.escape(value.toString) + "\"")
    }
    params.foreach { case (key, value) =>
      result = result.replaceAll("\\$" + key + "\\$", value.toString)
    }
    result
  }

}
