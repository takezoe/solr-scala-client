package jp.sf.amateras.solr.scala.async

import java.io.{ByteArrayOutputStream, InputStream}

import scala.compat.Platform

/**
 * @author steven
 *
 */
class UpdatableInputStream extends InputStream {
    private var finished = false
    private val baos = new ByteArrayOutputStream()

    def appendBytes(bytes: Array[Byte]) = this.synchronized {
        baos.write(bytes)
        this.notifyAll()
    }

    def finishedAppending() = this.synchronized {
        finished = true
        this.notifyAll()
    }

    private def dequeue(max: Int): Option[Array[Byte]] = this.synchronized {
        while (baos.size() == 0 && !finished) this.wait()

        if (baos.size() == 0 && finished)
            None
        else {
            val bytes = baos.toByteArray

            baos.reset()

            if (bytes.length <= max)
                Some(bytes)
            else {
                val ret = new Array[Byte](max)
                Platform.arraycopy(bytes, 0, ret, 0, max)
                baos.write(bytes, max, bytes.length - max)
                Some(ret)
            }
        }
    }

    override def read(): Int = {
        val arr = new Array[Byte](1)
        read(arr)
        arr(0)
    }

    override def read(b: Array[Byte], off: Int, len: Int): Int = {
        if (b == null)
            throw new NullPointerException
        else if (off < 0 || len < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException
        else if (len == 0)
            0
        else {
            dequeue(len) match {
                case None ⇒ -1
                case Some(bytes) ⇒
                    Platform.arraycopy(bytes, 0, b, off, bytes.length)
                    bytes.length
            }
        }
    }
}
