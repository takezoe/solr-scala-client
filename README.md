solr-scala-client
=================

The simple [Apache Solr](http://lucene.apache.org/solr/) client for Scala.
This is based on the SolrJ and provides optimal interface for Scala.

Add the following dependency into your build.sbt to use solr-scala-client.

```scala
resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

libraryDependencies += "jp.sf.amateras.solr.scala" %% "solr-scala-client" % "0.0.4"
```

This is a simplest example to show usage of solr-scala-client.

```scala
import jp.sf.amateras.solr.scala._

val client = new SolrClient("http://localhost:8983/solr")

// register
client
  .add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
  .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X220"))
  .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X121e"))
  .commit

// query
val result: = client.query("name: %name%")
  .fields("id", "manu", "name")
  .sortBy("id", Order.asc)
  .getResultAsMap(Map("name" -> "ThinkPad"))

result.documents.foreach { doc: Map[String, Any] =>
  println("id: " + doc("id"))
  println("  manu: " + doc("manu"))
  println("  name: " + doc("name"))
}
```

It's also possible to use the case class as the search result and parameters instead of Map.

```scala
// query
val result = client.query("name: %name%")
  .fields("id", "manu", "name")
  .sortBy("id", Order.asc)
  .getResultAs[Product](Param(name = "ThinkPad"))

result.documents.foreach { doc: Product =>
  println("id: " + product.id)
  println("  manu: " + product.manu)
  println("  name: " + product.name)
}
```

This project has been built continuously by [BuildHive](https://buildhive.cloudbees.com/view/My%20Repositories/job/takezoe/job/solr-scala-client/).

Query Syntax
--------

Following notations are available to embed variables to the query:

* %VAR_NAME% : place holder to set a single word (parameter would be escaped)
* ?VAR_NAME? : place holder to set an expression (&, | and ! are available in an expression)
* $VAR_NAME$ : string replacement (parameter would be not escaped)

See examples of parameterized queries and assembled Solr queries.

```scala
// %VAR_NAME% (Single keyword)
client.query("name: %name%").getResultAsMap(Map("name" -> "ThinkPad X201s"))
  // => name:"ThinkPad X201s"

// $VAR_NAME$ (String replacement)
client.query("name: $name$").getResultAsMap(Map("name" -> "ThinkPad AND X201s"))
  // => name:ThinkPad AND X201s

// ?VAR_NAME? (Expression)
client.query("name: ?name?").getResultAsMap(Map("name" -> "ThinkPad & X201s"))
  // => name:("ThinkPad" AND "X201s")
```

TODO
--------

* Detailed query configuration

Release Notes
--------
### 0.0.5 - Not released yet

* ExpressionParser became pluggable and added GoogleExpressionParser as an optional implementation of ExpressionParser.
* Converts the full-width space to the half-width space in the expression before calling ExpressionParser.

### 0.0.4 - 16 Sep 2012

* Expanding expression to the Solr query by ?VAR_NAME? in SolrClient#query().
* Bug fix

### 0.0.3 - 16 Aug 2012

* Added case class support in update operations.
* Added commit() method to SolrClient.

### 0.0.2 - 27 May 2012

* Added initializer which configures SolrClient.
* Added basic authentication support as initializer.
* Added facet search support.
* Added case class support as query results and query parameters.

### 0.0.1 - 4 May 2012

* Initial public release.
