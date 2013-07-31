package jp.sf.amateras.solr.scala.async

import com.ning.http.client.AsyncCompletionHandler
import com.ning.http.client.Response
import com.ning.http.client.AsyncHttpClient
import scala.concurrent.Promise

object AsyncUtils {
  
  class CallbackHandler[T](httpClient: AsyncHttpClient, promise: Promise[T],
      success: Response => T) extends AsyncCompletionHandler[Unit] {
    override def onCompleted(response: Response): Unit = {
      try {
        promise.success(success(response))
      } finally {
        httpClient.closeAsynchronously()
      }
    }
        
    override def onThrowable(t: Throwable): Unit = {
      try {
        promise.failure(t)
      } finally {
        httpClient.closeAsynchronously()
      }
    }
  }
  
}