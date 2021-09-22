name := """reactive-http"""

version := "1.3"

scalaVersion := "2.13.6"
   
val akkaVersion = "2.6.16"
val akkaHttpVersion = "10.2.6"
libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed"  % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion