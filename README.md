solr-scala-client
=================

The simple [Apache Solr](http://lucene.apache.org/solr/) client for Scala.
This is based on the SolrJ and provides optimal interface for Scala.

    import jp.sf.amateras.solr.scala

    val client = new SolrClient("http://localhost:8983/solr")

    // register
    client
      .add(Map("id"->"001", "manu" -> "Lenovo", "name" -> "ThinkPad X201s"))
      .add(Map("id"->"002", "manu" -> "Lenovo", "name" -> "ThinkPad X202"))
      .add(Map("id"->"003", "manu" -> "Lenovo", "name" -> "ThinkPad X100e"))
    .commit

    // query
    val result: List[Map[String, Any]] =
      client.query("*:*")
        .fields("id", "manu", "name")
        .sortBy("id", Order.asc)
        .getResult

    result.foreach { doc: Map[String, Any] =>
      println("id: " + doc("id"))
      println("  manu: " + doc("manu"))
      println("  name: " + doc("name"))
    }

solr-scala-client is not released yet. It's still under development.

TODO
--------

* Registering and deleting document
* Mapping document to case class
* Flexible configuration to SolrServer
* Facet search