package jp.sf.amateras.solr.scala

import org.apache.solr.common._
import org.apache.solr.common.util._
import org.apache.solr.client.solrj._
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.UsernamePasswordCredentials

object SolrServerFactory {
  
  /**
   * Configure SolrClient for basic authentication.
   *
   * {{{
   * implicit val server = SolrServerFactory.basicAuth("username", "password")
   * val client = new SolrClient("http://localhost:8983/solr")
   * }}}
   */
  def basicAuth(username: String, password: String) = (url: String) => {
      val server = new CommonsHttpSolrServer(url)
      
      val cred = new UsernamePasswordCredentials(username, password)
      val jurl = new java.net.URL(server.getBaseURL())

      val client = server.getHttpClient()
      client.getState().setCredentials(new AuthScope(jurl.getHost(), jurl.getPort(), AuthScope.ANY_REALM), cred)
      client.getParams().setAuthenticationPreemptive(true)
      
      server
  }
  
  /**
   * Provides the dummy SolrServer for unit testing.
   * 
   * {{{
   * implicit val server = SolrServerFactory.dummt { request =>
   *   println(request.getMethod)
   *   println(request.getPath)
   *   println(request.getParams)
   * }
   * val client = new SolrClient("http://localhost:8983/solr")
   * }}}
   */
  def dummy(listener: (SolrRequest) => Unit) = (url: String) => new SolrServer {
	def request(request: SolrRequest): NamedList[Object] = {
	  listener(request)
	  
	  val response =  new SimpleOrderedMap[Object]()
	  response.add("response", new SolrDocumentList())
	  
	  response
	}
  }
  
}