package com.github.takezoe.solr.scala.async

import java.io.IOException

import okhttp3.{Call, Callback, OkHttpClient, Response}

import scala.concurrent.Promise

object AsyncUtils {

  /**
   * A result handler implementation for AsyncHttpClient
   * which notifies the result of asynchronous request via Promise.
   */
  class CallbackHandler[T](httpClient: OkHttpClient, promise: Promise[T],
      success: Response => T = (x: Response) => ()) extends Callback {

    override def onFailure(call: Call, e: IOException): Unit = {
      promise.failure(e)
    }

    override def onResponse(call: Call, response: Response): Unit = try {
      promise.success(success(response))
    } finally {
      response.close()
    }
  }

}