package jp.sf.amateras.solr.scala

private[scala] class QueryTemplate(query: String) {

  def merge(params: Map[String, Any]): String = {
    var result = query
    params.foreach { case (key, value) =>
      result = result.replaceAll("%" + key + "%", "\"" + QueryTemplate.escape(value.toString) + "\"")
    }
    params.foreach { case (key, value) =>
      result = result.replaceAll("$" + key + "$", "\"" + value.toString + "\"")
    }
    result
  }

}

object QueryTemplate {

  private lazy val specialCharacters =
    Set('+', '-', '&', '|',  '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\')

  private def escape(value: String): String =
    value.toString.map { c =>
      c match {
        case _ if specialCharacters.contains(c) => Seq('\\', '\\', c)
        case _ => Seq(c)
      }
    }.flatten.mkString

}