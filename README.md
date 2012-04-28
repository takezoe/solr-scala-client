solr-scala-client
=================

Solr Client for Scala




    val client = new SolrClient("http://localhost:8983/solr")

    val result = client.query("*:*")
          .fields("id", "manu", "name")
          .sortBy("id", ORDER.asc)
          .getResult

    result.foreach { doc =>
      println("id: " + doc.getFieldValue("id"))
      println("  manu: " + doc.getFieldValue("manu"))
      println("  name: " + doc.getFieldValue("name"))
    }
