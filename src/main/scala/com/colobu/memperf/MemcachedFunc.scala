package com.colobu.memperf

trait MemcachedFunc {
  def add: Unit
  def syncGet: Unit
  def asyncGet: Unit
  def gets: Unit
  def asyncSet: Unit

  def stop: Unit
}
