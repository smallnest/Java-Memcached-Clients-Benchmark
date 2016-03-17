package com.colobu.memperf


import com.google.common.net.HostAndPort
import com.google.common.util.concurrent.{Futures, ListenableFuture}
import com.spotify.folsom.MemcacheClientBuilder
import com.spotify.folsom.client.{NoopMetrics, Utils}

import scala.collection.JavaConverters._
import scala.collection.mutable

class Folsom(count: Int = 100000, address: java.util.List[HostAndPort] = Vector(HostAndPort.fromString("127.0.0.1:11211")).asJava, bytes: Array[Byte]) extends MemcachedFunc {
  val builder = MemcacheClientBuilder.newByteArrayClient()
    .withAddresses(address)
    .withConnections(5)
    .withMaxOutstandingRequests(1000000)
    .withMetrics(NoopMetrics.INSTANCE)
    .withRetry(false)
    .withReplyExecutor(Utils.SAME_THREAD_EXECUTOR)
    .withRequestTimeoutMillis(10 * 1000);

  val client = builder.connectBinary()

  override def add: Unit = {
    for (i <- 1 to count) {
      try {
        client.add("key" + i, bytes, 3600)
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  override def syncGet: Unit = {
    for (i <- 1 to count) {
      try {
        client.get("key" + i).get
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  override def asyncGet: Unit = {
    val futures = mutable.ListBuffer[ListenableFuture[Array[Byte]]]()
    for (i <- 1 to count) {
      try {
        futures += client.get("key" + i)
      } catch {
        case e => e.printStackTrace()
      }
    }

    val c = Futures.successfulAsList(futures.asJava).get().size()
  }

  override def gets: Unit = {
    val futures = mutable.ListBuffer[ListenableFuture[java.util.List[Array[Byte]]]]()

    for (i <- 1 to count / 100) {
      val start = (i - 1) * 100 + 1
      val keys = start to (start + 100) map ("key" + _)
      try {
        futures += client.get(keys.asJava)
      } catch {
        case e => e.printStackTrace()
      }
    }

    val c = Futures.successfulAsList(futures.asJava).get().size()
  }

  override def asyncSet: Unit = {
    for (i <- 1 to count) {
      try {
        client.set("key" + i, bytes, 3600)
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  def stop = client.shutdown()
}
