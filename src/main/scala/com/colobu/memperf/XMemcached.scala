package com.colobu.memperf

import net.rubyeye.xmemcached.XMemcachedClientBuilder
import net.rubyeye.xmemcached.command.BinaryCommandFactory
import net.rubyeye.xmemcached.utils.AddrUtil

import scala.collection.JavaConverters._


class XMemcached(count: Int = 100000, address: String = "127.0.0.1:11211", bytes: Array[Byte]) extends MemcachedFunc {
  val builder = new XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:11211"));
  builder.setCommandFactory(new BinaryCommandFactory());
  builder.setConnectionPoolSize(100)
  val client = builder.build();

  override def add: Unit = {
    for (i <- 1 to count) {
      //client.add("key" + i, 3600, i.toString)
      try {
        client.addWithNoReply("key" + i, 3600, bytes)
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  override def syncGet: Unit = {
    for (i <- 1 to count) {
      try {
        client.get[Array[Byte]]("key" + i)
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  override def asyncGet: Unit = ???

  override def gets: Unit = {
    for (i <- 1 to count / 100) {
      val start = (i - 1) * 100 + 1
      val keys = start to (start + 100) map ("key" + _)
      try {
        client.gets[Array[Byte]](keys.asJava)
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  override def asyncSet: Unit = {
    for (i <- 1 to count) {
      //java.lang.IllegalStateException: No permit for noreply operation
      //client.setWithNoReply("key" + i, 3600, i.toString)
      try {
        client.set("key" + i, 3600, bytes)
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  def stop = client.shutdown()
}
