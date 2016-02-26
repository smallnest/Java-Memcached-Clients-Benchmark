package com.colobu.memperf


import com.google.common.net.HostAndPort
import com.google.common.util.concurrent.{Futures, ListenableFuture}
import com.spotify.folsom.MemcacheClientBuilder
import com.spotify.folsom.client.{NoopMetrics, Utils}

import scala.collection.JavaConverters._
import scala.collection.mutable

class Folsom(count:Int = 100000, address: java.util.List[HostAndPort] = Vector(HostAndPort.fromString("127.0.0.1:11211")).asJava, connections: Int = 1) extends MemcachedFunc {
  val builder = MemcacheClientBuilder.newStringClient()
    .withAddresses(address)
    .withConnections(connections)
    .withMaxOutstandingRequests(10000)
    .withMetrics(NoopMetrics.INSTANCE)
    .withRetry(false)
    .withReplyExecutor(Utils.SAME_THREAD_EXECUTOR)
    .withRequestTimeoutMillis(10 * 1000);

  val client = builder.connectBinary()

  override def add: Unit = {
    for (i <- 1 to count) {
      client.add("key" + i, i.toString, 3600)
    }
  }

  override def syncGet: Unit = {
    for (i <- 1 to count) {
      client.get("key" + i).get
    }
  }

  override def asyncGet: Unit = {
    val futures = mutable.ListBuffer[ListenableFuture[String]]()
    for (i <- 1 to count) {
      futures += client.get("key" + i)
    }

    val c = Futures.successfulAsList(futures.asJava).get().size()
  }

  override def gets: Unit = {
    val futures = mutable.ListBuffer[ListenableFuture[java.util.List[String]]]()

    for (i <- 1 to count/100) {
      val start = (i - 1) * 100 + 1
      val keys =  start to (start + 100) map("key" + _)
      futures += client.get(keys.asJava)
    }

    val c = Futures.successfulAsList(futures.asJava).get().size()
  }

  override def asyncSet: Unit = {
    for (i <- 1 to count) {
      client.set("key" + i, i.toString, 3600)
    }
  }

  def stop = client.shutdown()
}
