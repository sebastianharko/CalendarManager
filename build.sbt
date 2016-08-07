name := "calendar"

version := "0.1"

scalaVersion := "2.11.8"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.4.9-RC2"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9-RC2"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-persistence" % "2.4.9-RC2"

libraryDependencies +=
  "de.heikoseeberger" %% "akka-http-json4s" % "1.8.0"

libraryDependencies +=
  "org.scalaz" %% "scalaz-core" % "7.2.4"

libraryDependencies +=
  "ch.qos.logback" % "logback-classic" % "1.1.7"

libraryDependencies +=
  "org.json4s" %% "json4s-jackson" % "3.2.9"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.9-RC2"


resolvers += Resolver.jcenterRepo

libraryDependencies += "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.6-RC1"