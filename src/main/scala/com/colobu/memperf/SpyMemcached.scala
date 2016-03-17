package com.colobu.memperf

import net.spy.memcached._
import net.spy.memcached.internal.{BulkFuture, GetFuture}

import scala.collection.JavaConverters._
import scala.collection.mutable

class SpyMemcached(count: Int = 100000, address: String = "127.0.0.1:11211", bytes: Array[Byte]) extends MemcachedFunc {
  val client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(address))

  override def add: Unit = {
    for (i <- 1 to count) {
      client.add("key" + i, 3600, bytes)
    }
  }

  override def syncGet: Unit = {
    for (i <- 1 to count) {
      try {
        client.get("key" + i)
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  override def asyncGet: Unit = {
    val futures = mutable.ListBuffer[GetFuture[Object]]()
    for (i <- 1 to count) {
      try {
        futures += client.asyncGet("key" + i)
      } catch {
        case e => e.printStackTrace()
      }
    }

    //TODO: wait all futures completed
  }

  override def gets: Unit = {
    val futures = mutable.ListBuffer[BulkFuture[java.util.Map[String, AnyRef]]]()

    for (i <- 1 to count / 100) {
      val start = (i - 1) * 100 + 1
      val keys = start to (start + 100) map ("key" + _)
      try {
        futures += client.asyncGetBulk(keys.asJava)
      } catch {
        case e => e.printStackTrace()
      }
    }

    //TODO: wait all futures completed
  }

  override def asyncSet: Unit = {
    for (i <- 1 to count) {
      try {
        client.set("key" + i, 3600, bytes)
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  def stop = client.shutdown()
}
