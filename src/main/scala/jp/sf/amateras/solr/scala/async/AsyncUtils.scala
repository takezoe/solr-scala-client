package jp.sf.amateras.solr.scala.async

import com.ning.http.client.AsyncCompletionHandler
import com.ning.http.client.Response
import com.ning.http.client.AsyncHttpClient

object AsyncUtils {
  
  class CallbackHandler(success: Response => Unit, failure: Throwable => Unit)
                       (implicit httpClient: AsyncHttpClient) extends AsyncCompletionHandler[Unit] {
    override def onCompleted(response: Response): Unit = {
      try {
        success(response)
      } finally {
        httpClient.closeAsynchronously()
      }
    }
        
    override def onThrowable(t: Throwable): Unit = {
      try {
        failure(t)
      } finally {
        httpClient.closeAsynchronously()
      }
    }
  }
  
  val defaultSuccessHandler = (r: Response) => {}
  
  val defaultFailureHandler = (t: Throwable) => t.printStackTrace
  
}