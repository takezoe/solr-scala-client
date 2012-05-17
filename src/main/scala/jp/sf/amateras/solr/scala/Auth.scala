package jp.sf.amateras.solr.scala
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer

object Auth {

  /**
   * Configure SolrClient for basic authentication.
   *
   * {{{
   * val client = new SolrClient("http://localhost:8983/solr", Auth.basic("username", "password"))
   * }}}
   */
  def basic(username: String, password: String) = { server: CommonsHttpSolrServer =>
      val cred = new UsernamePasswordCredentials(username, password)
      val url = new java.net.URL(server.getBaseURL())

      val client = server.getHttpClient()
      client.getState().setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), cred)
      client.getParams().setAuthenticationPreemptive(true)
  }

}
