name := """reactive-http"""

version := "1.2"

scalaVersion := "2.12.3"
   
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "com.typesafe.akka" %% "akka-remote" % "2.5.4",
  "com.typesafe.akka" %% "akka-http" % "10.0.10")


