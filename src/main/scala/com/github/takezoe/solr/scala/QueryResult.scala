package com.github.takezoe.solr.scala

/**
 * The result of query which is executed by QueryBuilder#getResultAsMap().
 *
 * @param numFound the total number of hits
 * @param documents the list of documents which are matched to the query
 * @param groups the result of the grouped query which were specified by QueryBuilder#groupBy()
 * @param facetFields the facet count of fields which were specified by QueryBuilder#facetFields()
 */
case class MapQueryResult(
    numFound: Long,
    numGroupsFound: Long,
    documents: List[DocumentMap],
    groups: Map[String, List[Group]],
    facetFields: Map[String, Map[String, Long]],
    qTime:Int)

/**
 * The result of query which is executed by QueryBuilder#getResultAs().
 *
 * @param numFound the total number of hits
 * @param documents the list of documents which are matched to the query
 * @param facetFields the facet count of fields which were specified by QueryBuilder#facetFields()
 * @tparam T
 */
case class CaseClassQueryResult[T](
    numFound: Long,
    documents: List[T],
    facetFields: Map[String, Map[String, Long]])

case class Group(
    value: String,
    numFound: Long,
    documents: List[DocumentMap])