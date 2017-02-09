name := "scala-vat"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.beachape" %% "enumeratum" % "1.5.6",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "com.opencsv" % "opencsv" % "3.8",
  "com.typesafe.akka" %% "akka-actor" % "2.4.16",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.16"

)

