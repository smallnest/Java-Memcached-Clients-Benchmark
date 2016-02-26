package com.colobu.memperf

import java.util.concurrent.atomic.AtomicInteger

import com.google.common.util.concurrent.{Futures, JdkFutureAdapters, ListenableFuture}
import net.spy.memcached._
import net.spy.memcached.internal.{GetCompletionListener, BulkFuture, GetFuture}

import scala.collection.JavaConverters._
import scala.collection.mutable

class SpyMemcached (count:Int = 100000, address:String = "127.0.0.1:11211", connections: Int = 100) extends MemcachedFunc {
  val client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(address))

  override def add: Unit = {
    for (i <- 1 to count) {
      client.add("key" + i, 3600, i.toString)
    }
  }

  override def syncGet: Unit = {
    for (i <- 1 to count) {
      client.get("key" + i)
    }
  }

  override def asyncGet: Unit = {
    val futures = mutable.ListBuffer[GetFuture[Object]]()
    for (i <- 1 to count) {
      futures += client.asyncGet("key" + i)
    }

    //TODO: wait all futures completed
  }

  override def gets: Unit = {
    val futures = mutable.ListBuffer[BulkFuture[java.util.Map[String, AnyRef]]]()

    for (i <- 1 to count/100) {
      val start = (i - 1) * 100 + 1
      val keys =  start to (start + 100) map("key" + _)
      futures += client.asyncGetBulk(keys.asJava)
    }

    //TODO: wait all futures completed
  }

  override def asyncSet: Unit = {
    for (i <- 1 to count) {
      client.set("key" + i, 3600, i.toString)
    }
  }

  def stop = client.shutdown()
}
