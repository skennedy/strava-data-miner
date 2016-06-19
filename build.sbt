name := "strava-data-miner"

organization := "skennedy"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.7",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.7",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "kiambogo" % "scrava_2.11" % "1.1.5",

  "org.scalatest" % "scalatest_2.11" % "2.2.5" % "test",
  "org.scalamock" % "scalamock-scalatest-support_2.11" % "3.2" % "test",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.7" % "test",
  "com.miguno.akka" % "akka-mock-scheduler_2.11" % "0.4.0" % "test"

)
