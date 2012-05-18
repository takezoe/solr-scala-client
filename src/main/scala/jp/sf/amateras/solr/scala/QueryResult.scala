package jp.sf.amateras.solr.scala

/**
 * The result of query which is executed by QueryBuilder#getResult().
 *
 * @param documents the list of documents which are matched to the query
 * @param facetFields the facet count of fields which were specified by QueryBuilder#facetFields()
 */
case class QueryResult(
    documents: List[Map[String, Any]],
    facetFields: Map[String, Map[String, Long]])

