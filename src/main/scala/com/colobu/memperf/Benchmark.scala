package com.colobu.memperf

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import sys.process._

object Benchmark extends App {
  val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
  root.setLevel(Level.ERROR)

  def test(func: => Unit, s: String) = {
    var t = System.nanoTime()
    func
    t = System.nanoTime() - t
    printf(s, t / 1000000)
  }

  def testWithThreads(func: => Unit, threadNum: Int = 100): Unit = {
    val futures = mutable.ListBuffer[Future[Unit]]()

    for (_ <- 1 to threadNum) {
      val f: Future[Unit] = Future {
        func
      }
      futures += f
    }

    Await.result(Future.sequence(futures), 30.minutes)
  }

  var testType: String = null
  if (args.length > 0)
    testType = args(0)

  var count = 10000
  if (args.length > 1)
    count = args(1).toInt

  val data = List(new Array[Byte](32), new Array[Byte](512), new Array[Byte](1024))

  for (d <- data) {
    "service memcached restart"!

    if (testType == null || testType == "folsom" || testType == "all") {
      println(s"======== Folsom Test with ${d.length} bytes ========")
      val folsom = new Folsom(count = count, bytes = d)
      test(testWithThreads(folsom.add), "Folsom add: %d ms\n")
      test(testWithThreads(folsom.syncGet), "Folsom sync get: %d ms\n")
      test(testWithThreads(folsom.asyncGet), "Folsom async get: %d ms\n")
      test(testWithThreads(folsom.gets), "Folsom gets: %d ms\n")
      test(testWithThreads(folsom.asyncSet), "Folsom async set: %d ms\n")
      folsom.stop
    }

    "service memcached restart"!

    if (testType == null || testType == "xmemcached" || testType == "all") {
      println(s"======== XMemcached Test with ${d.length} bytes  ========")
      val xMemcached = new XMemcached(count = count, bytes = d)
      test(testWithThreads(xMemcached.add), "XMemcached add: %d ms\n")
      test(testWithThreads(xMemcached.syncGet), "XMemcached sync get: %d ms\n")
      test(testWithThreads(xMemcached.gets), "XMemcached gets: %d ms\n")
      test(testWithThreads(xMemcached.asyncSet), "XMemcached async set: %d ms\n")
      xMemcached.stop
    }

    "service memcached restart"!

    if (testType == null || testType == "spy" || testType == "all") {
      println(s"======== SpyMemcached Test  with ${d.length} bytes ========")
      val spy = new SpyMemcached(count = count, bytes = d)
      test(testWithThreads(spy.add), "SpyMemcached add: %d ms\n")
      test(testWithThreads(spy.syncGet), "SpyMemcached sync get: %d ms\n")
      test(testWithThreads(spy.asyncGet), "SpyMemcached async get: %d ms\n")
      test(testWithThreads(spy.gets), "SpyMemcached gets: %d ms\n")
      test(testWithThreads(spy.asyncSet), "SpyMemcached async set: %d ms\n")
      spy.stop
    }
  }


}

