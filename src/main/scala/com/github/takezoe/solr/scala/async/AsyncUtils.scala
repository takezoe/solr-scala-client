package com.github.takezoe.solr.scala.async

import org.asynchttpclient._
import scala.concurrent.Promise

object AsyncUtils {
  
  /**
   * A result handler implementation for AsyncHttpClient
   * which notifies the result of asynchronous request via Promise.
   */
  class CallbackHandler[T](httpClient: AsyncHttpClient, promise: Promise[T],
      success: Response => T = (x: Response) => ()) extends AsyncCompletionHandler[Unit] {
    
    override def onCompleted(response: Response): Unit = {
      promise.success(success(response))
    }
        
    override def onThrowable(t: Throwable): Unit = {
      promise.failure(t)
    }
  }
  
}