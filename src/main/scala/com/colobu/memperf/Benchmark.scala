package com.colobu.memperf

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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

  val testType = args(0)

  if (testType == null || testType == "folsom" || testType == "all") {
    println("======== Folsom Test ========")
    val folsom = new Folsom
    test(testWithThreads(folsom.add), "Folsom add: %d ms\n")
    test(testWithThreads(folsom.syncGet), "Folsom sync get: %d ms\n")
    test(testWithThreads(folsom.asyncGet), "Folsom async get: %d ms\n")
    test(testWithThreads(folsom.gets), "Folsom gets: %d ms\n")
    test(testWithThreads(folsom.asyncSet), "Folsom async set: %d ms\n")
    folsom.stop
  }


  if (testType == null || testType == "xmemcached" || testType == "all") {
    println("======== XMemcached Test ========")
    val xMemcached = new XMemcached
    test(testWithThreads(xMemcached.add), "XMemcached add: %d ms\n")
    test(testWithThreads(xMemcached.syncGet), "XMemcached sync get: %d ms\n")
    test(testWithThreads(xMemcached.gets), "XMemcached gets: %d ms\n")
    test(testWithThreads(xMemcached.asyncSet), "XMemcached async set: %d ms\n")
    xMemcached.stop
  }

  if (testType == null || testType == "spy" || testType == "all") {
    println("======== SpyMemcached Test ========")
    val spy = new SpyMemcached
    test(testWithThreads(spy.add), "SpyMemcached add: %d ms\n")
    test(testWithThreads(spy.syncGet), "SpyMemcached sync get: %d ms\n")
    test(testWithThreads(spy.asyncGet), "SpyMemcached async get: %d ms\n")
    test(testWithThreads(spy.gets), "SpyMemcached gets: %d ms\n")
    test(testWithThreads(spy.asyncSet), "SpyMemcached async set: %d ms\n")
    spy.stop
  }

}

