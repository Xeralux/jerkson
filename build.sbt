organization := "com.codahale"

name := "jerkson"

version := "0.6.0-SNAPSHOT"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.0.6",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.6")
