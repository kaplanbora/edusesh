name := """Edusesh"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.4"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.194"
libraryDependencies += "org.postgresql" % "postgresql" % "42.1.3"
libraryDependencies += "commons-validator" % "commons-validator" % "1.6"
libraryDependencies += "com.github.nscala-money" %% "nscala-money" % "0.13.0"
libraryDependencies += "com.typesafe.play" %% "play-slick" %  "3.0.2"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"
libraryDependencies += "io.igl" %% "jwt" % "1.2.2"
libraryDependencies += "io.github.scala-hamsters" %% "hamsters" % "2.5.0"

