name := """Kaplan Academy"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.194"
libraryDependencies += "org.postgresql" % "postgresql" % "42.1.3"
libraryDependencies += "commons-validator" % "commons-validator" % "1.6"
libraryDependencies += "com.github.nscala-money" %% "nscala-money" % "0.13.0"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.16.0"
libraryDependencies += "com.typesafe.play" %% "play-slick" %  "3.0.2"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.2"
//libraryDependencies += "com.typesafe.play" % "play-json-joda_2.11" % "2.6.6"


