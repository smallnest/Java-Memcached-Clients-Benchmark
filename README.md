# Java-Memcached-Clients-Benchmark
Benchmark for Java Memcached clients such as SpyMemcached, XMemcached and folsom


## Test Result
======== Folsom Test ========
Folsom add: 10673 ms
Folsom sync get: 297206 ms
Folsom async get: 33033 ms
Folsom gets: 78727 ms
Folsom async set: 11028 ms

======== XMemcached Test ========
XMemcached add: 311259 ms
XMemcached sync get: 289187 ms
XMemcached gets: 79559 ms
XMemcached sync set: 314287 ms (async set "setWithNoreply" throws java.lang.IllegalStateException: No permit for noreply operation)

======== SpyMemcached Test ========
SpyMemcached add: 56147 ms
SpyMemcached sync get: 552638 ms
SpyMemcached async get: 42703 ms
SpyMemcached gets: 11728 ms
SpyMemcached async set: 48141 ms