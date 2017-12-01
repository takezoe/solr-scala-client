package com.github.takezoe.solr.scala

import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.solr.client.solrj._
import org.apache.solr.client.solrj.{SolrClient => ApacheSolrClient}
import org.apache.solr.client.solrj.impl.{HttpSolrClient => ApacheHttpSolrClient}
import org.apache.solr.client.solrj.impl.{CloudSolrClient => ApacheCloudSolrClient}
import org.apache.solr.common._
import org.apache.solr.common.util._

object SolrClientFactory {

  /**
   * Configure HttpSolrClient for basic authentication.
   *
   * {{{
   * implicit val solr = SolrClientFactory.basicAuth("username", "password")
   * val client = new SolrClient("http://localhost:8983/solr")
   * }}}
   */
  def basicAuth(username: String, password: String) = (url: String) => {
    val client = new ApacheHttpSolrClient.Builder(url).build()
    val jurl = new java.net.URL(client.getBaseURL)

    val httpClient = client.getHttpClient.asInstanceOf[DefaultHttpClient]

    httpClient.getParams.setBooleanParameter("http.authentication.preemptive", true)
    httpClient
      .getCredentialsProvider
      .setCredentials(
        new AuthScope(jurl.getHost, jurl.getPort, AuthScope.ANY_REALM),
        new UsernamePasswordCredentials(username, password)
      )

    client
  }
  
  /**
   * Provides the dummy HttpSolrClient for unit testing.
   * 
   * {{{
   * implicit val solr = SolrClientFactory.dummy { request =>
   *   println(request.getMethod)
   *   println(request.getPath)
   *   println(request.getParams)
   * }
   * val client = new SolrClient("http://localhost:8983/solr")
   * }}}
   */
  def dummy(listener: (SolrRequest[_ <: SolrResponse]) => Unit) = (url: String) => new ApacheSolrClient {
    def close() = {}
    override def request(request: SolrRequest[_ <: SolrResponse], collection: String): NamedList[AnyRef] = {
      listener(request)

      val response =  new SimpleOrderedMap[Object]()
      response.add("response", new SolrDocumentList())

      response
    }
  }

  /**
   * Provides a client for CloudSolr.
   *
   * {{{
   * implicit val solr = SolrClientFactory.cloud()
   * val client = new SolrClient("zkHost1,zkHost2,zkHost3:2182/solr")
   * }}}
   */
  def cloud() = (url: String) => (new ApacheCloudSolrClient.Builder).withZkHost(url).build()

  /**
   * Provides a client for CloudSolr with authentication.
   *
   * {{{
   * implicit val solr = SolrClientFactory.cloud("username", "password")
   * val client = new SolrClient("zkHost1,zkHost2,zkHost3:2182/solr")
   * }}}
   */
  def cloud(username: String, password: String) = (url: String) => {
    val client = (new ApacheCloudSolrClient.Builder).withZkHost(url).build()
    val httpClient = client.getHttpClient.asInstanceOf[DefaultHttpClient]

    httpClient.getParams.setBooleanParameter("http.authentication.preemptive", true)
    httpClient
      .getCredentialsProvider
      .setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(username, password))

    client
  }

}
