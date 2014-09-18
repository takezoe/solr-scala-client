package jp.sf.amateras.solr.scala

/**
 * The result of query which is executed by QueryBuilder#getResultAsMap().
 *
 * @param numFound the total number of hits
 * @param documents the list of documents which are matched to the query
 * @param facetFields the facet count of fields which were specified by QueryBuilder#facetFields()
 * @param facetDates the facet count of date fields which were specified by QueryBuilder#facetDates()
 */
case class MapQueryResult(
    numFound: Long,
    documents: List[Map[String, Any]],
    facetFields: Map[String, Map[String, Long]],
    facetDates: Map[String, Map[String, Long]])

case class CaseClassQueryResult[T](
    numFound: Long,
    documents: List[T],
    facetFields: Map[String, Map[String, Long]],
    facetDates: Map[String, Map[String, Long]])

