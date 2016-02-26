resolvers ++= Seq( DefaultMavenRepository, Resolver.sonatypeRepo("public"),
  "Bintray sbt plugin releases"  at "http://dl.bintray.com/sbt/sbt-plugin-releases/")

name := "MemcacheClient_Benchmark"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "net.spy" % "spymemcached" % "2.12.0",
  "com.googlecode.xmemcached" % "xmemcached" % "2.0.0",
  "com.spotify" % "folsom" % "0.7.1",
  "ch.qos.logback" % "logback-classic" % "1.1.5"
)

assemblyJarName in assembly := "memtest.jar"

test in assembly := {}

mainClass in assembly := Some("com.colobu.memperf.Benchmark")