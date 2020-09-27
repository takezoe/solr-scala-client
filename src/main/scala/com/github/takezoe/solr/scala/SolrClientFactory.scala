package com.github.takezoe.solr.scala

import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClientBuilder}
import org.apache.solr.client.solrj._
import org.apache.solr.client.solrj.{SolrClient => ApacheSolrClient}
import org.apache.solr.client.solrj.impl.{HttpSolrClient => ApacheHttpSolrClient}
import org.apache.solr.client.solrj.impl.{CloudSolrClient => ApacheCloudSolrClient}
import org.apache.solr.common._
import org.apache.solr.common.util._
import java.{util => ju}

object SolrClientFactory {

  /**
   * Configure HttpSolrClient for basic authentication.
   *
   * {{{
   * implicit val solr = SolrClientFactory.basicAuth("username", "password")
   * val client = new SolrClient("http://localhost:8983/solr")
   * }}}
   */
  def basicAuth(username: String, password: String): String => ApacheHttpSolrClient = (url: String) => {

    val jurl = new java.net.URL(url)
    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials(username, password)
    provider.setCredentials(
      new AuthScope(jurl.getHost, jurl.getPort, AuthScope.ANY_REALM),
      credentials)
    val client = HttpClientBuilder.create.setDefaultCredentialsProvider(provider).build

    new ApacheHttpSolrClient.Builder(url).withHttpClient(client).build()

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
  def dummy(listener: (SolrRequest[_ <: SolrResponse]) => Unit): String => ApacheSolrClient = (url: String) => new ApacheSolrClient {
    def close(): Unit = {}
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
  def cloud(): String => ApacheCloudSolrClient = (url: String) => new ApacheCloudSolrClient.Builder(ju.Arrays.asList(url), ju.Optional.empty()).build()

  /**
   * Provides a client for CloudSolr with authentication.
   *
   * {{{
   * implicit val solr = SolrClientFactory.cloud("username", "password")
   * val client = new SolrClient("zkHost1,zkHost2,zkHost3:2182/solr")
   * }}}
   */
  def cloud(username: String, password: String): String => ApacheCloudSolrClient = (url: String) => {
    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials(username, password)
    provider.setCredentials(AuthScope.ANY, credentials)
    val client = HttpClientBuilder.create.setDefaultCredentialsProvider(provider).build
    new ApacheCloudSolrClient.Builder(ju.Arrays.asList(url), ju.Optional.empty()).withHttpClient(client).build()
  }

}
