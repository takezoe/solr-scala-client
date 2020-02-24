solr-scala-client [![Build Status](https://travis-ci.org/takezoe/solr-scala-client.svg?branch=master)](https://travis-ci.org/takezoe/solr-scala-client) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.takezoe/solr-scala-client_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.takezoe/solr-scala-client_2.12) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/takezoe/solr-scala-client/blob/master/LICENSE)
=================

The simple [Apache Solr](http://lucene.apache.org/solr/) client for Scala.
This is based on the SolrJ and provides optimal interface for Scala.

Add the following dependency into your `build.sbt` to use solr-scala-client.

```scala
libraryDependencies += "com.github.takezoe" %% "solr-scala-client" % "0.0.23"
```

If you want to test SNAPSHOT version, add the following dependency instead of above:

```scala
resolvers += "sonatype-oss-snapshot" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.github.takezoe" %% "solr-scala-client" % "x.x.x-SNAPSHOT"
```

This is a simplest example to show usage of solr-scala-client.

```scala
import com.github.takezoe.solr.scala._

val client = new SolrClient("http://localhost:8983/solr")

// register
client
  .add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
  .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X220"))
  .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X121e"))
  .commit

// query
val result = client.query("name: %name%")
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

result.documents.foreach { product: Product =>
  println("id: " + product.id)
  println("  manu: " + product.manu)
  println("  name: " + product.name)
}
```

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

Highlight
--------

Configure the query to return the highlighted content by ```QueryBuilder#highlight()```.
The highlighted content is set as the "highlight" property to the Map or the case class.

```scala
val result = client.query("content: Scala")
  // NOTE: unique key field is required.
  .fields("id")
  // Specify the highlighted field, prefix and postfix (prefix and postfix are optional).
  .highlight("content", "<strong>", "</strong>")
  .getResultAsMap()

result.documents.foreach { doc: Map[String, Any] =>
  println("id: " + doc("id"))
  println(doc("highlight")) // highlighted content is set as the "highlight" property
}
```
solr-scala-client expects that the unique key is "id".
If your schema has the different field as the unique key, you can specify the unique key name as following:

```scala
val result = client.query("content: Scala")
  .id("documentId") // Specify the unique key name
  .fields("documentId")
  .highlight("content", "<strong>", "</strong>")
  .getResultAsMap()
```

Asynchronous API
--------

solr-scala-client has also asynchronous API based on [AsyncHttpCleint](https://github.com/AsyncHttpClient/async-http-client).

```scala
val client = new AsyncSolrClient("http://localhost:8983/solr")

// Register
client
  .register(Map("id" -> "005", "name" -> "ThinkPad X1 Carbon", "manu" -> "Lenovo"))
  .onComplete{
    case Success(x) => println("registered!")
    case Failure(t) => t.printStackTrace()
  }

// Query
client.query("name:%name%")
  .fields("id", "manu", "name")
  .facetFields("manu")
  .sortBy("id", Order.asc)
  .getResultAsMap(Map("name" -> "ThinkPad X201s"))
  .onComplete {
    case Success(x) => println(x)
    case Failure(t) => t.printStackTrace()
  }
```

See more example at [AsyncSolrClientSample.scala](https://github.com/takezoe/solr-scala-client/blob/master/src/main/scala/com/github/takezoe/solr/scala/sample/AsyncSolrClientSample.scala).

Release Notes
-------------
### 0.0.23 - 24 Feb 2020

* Facet Pivot Fields support
* Update dependent libraries

### 0.0.22 - 11 Dec 2019

* Spatial parameters support

### 0.0.21 - 22 Jun 2019

* Scala 2.13.0 support

### 0.0.20 - 13 Jan 2019

* Scala 2.13.0-M5 support
* Support specifying collection with transaction

### 0.0.19 - 4 Jun 2018

* Add support for grouping and qTime in the response
* Allow batch processing with a specific collection

### 0.0.18 - 15 Feb 2018

* Fix response leaking bug

### 0.0.17 - 5 Dec 2017

* Upgrade to SolrJ-7.1.0
* Switch backend to OkHttp from async-http-client
* Allow specifying collection name when building query
* Add implementation of CloudSolrClient with and without authentication

### 0.0.16 - 18 Oct 2017

* Upgrade Scala and async-http-client

### 0.0.15 - 22 Nov 2016

* Scala 2.12 support and library updating

### 0.0.14 - 14 Aug 2016

* Small refactoring

### 0.0.13 - 13 Aug 2016

* Upgrade to SolrJ-6.1.0
* Change group id and package name to `com.github.takezoe`
* Publish to the Maven central repository

### 0.0.12 - 7 Feb 2015

* Add ```QueryBuilderBase#fq()```
* Add ```QueryBuilderBase#setRequestHandler()```
* Add date facet
* Support for streaming results

### 0.0.11 - 29 Mar 2014

* Add ```SolrClient#shutdown()```
* ```QueryBuilder``` became immutable
* Upgrade solrj version to 4.5.1

### 0.0.10 - 08 Feb 2014

* Fix escaping in string literal.

### 0.0.9 - 18 Dec 2013

* Bug fix

### 0.0.8 - 2 Aug 2013

* Added recommendation search support.
* Added ```rollback``` and ```withTransaction``` to ```SolrScalaClient```.
* Added Asynchronous API.

### 0.0.7 - 4 Apr 2013

* Add build for Scala 2.10
* Upgrade to SolrJ 4.2.0
* Support highlighting

### 0.0.6 - 22 Jan 2013

* Fixed some ```ExpressionParser``` bugs.

### 0.0.5 - 20 Nov 2012

* ```ExpressionParser``` became pluggable and added ```GoogleExpressionParser``` as an optional implementation of ```ExpressionParser```.
* Converts the full-width space to the half-width space in the expression before calling ExpressionParser.
* Introduced the SolrServer factory. ```Auth.basic``` moved to ```SolrServerFactory.basicAuth``` and ```SolrServerFactory.dummy``` for unit testing.

### 0.0.4 - 16 Sep 2012

* Expanding expression to the Solr query by ```?VAR_NAME?``` in ```SolrClient#query()```.
* Bug fix

### 0.0.3 - 16 Aug 2012

* Added case class support in update operations.
* Added ```commit()``` method to ```SolrClient```.

### 0.0.2 - 27 May 2012

* Added initializer which configures ```SolrClient```.
* Added basic authentication support as initializer.
* Added facet search support.
* Added case class support as query results and query parameters.

### 0.0.1 - 4 May 2012

* Initial public release.
