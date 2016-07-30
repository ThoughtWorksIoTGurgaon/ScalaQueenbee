import play.PlayImport._
import play.PlayScala
import sbtassembly.Plugin._
import AssemblyKeys._

name := "QueenBee"

version := "0.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

net.virtualvoid.sbt.graph.Plugin.graphSettings

assemblySettings

jarName in assembly := "QueenBee.jar"

mainClass in assembly := Some("play.core.server.NettyServer")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "JCenter repo" at "https://bintray.com/bintray/jcenter/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

resolvers += Resolver.jcenterRepo

mergeStrategy in assembly := {
  case PathList("play", "core", "server", "ServerWithStop.class") => MergeStrategy.last
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

libraryDependencies ++= Seq(
  ws exclude("commons-logging", "commons-logging"),
  jdbc,
  anorm,
  "org.scalatest" %% "scalatest" % "2.2.6",
  "net.sigusr" %% "scala-mqtt-client" % "0.6.0",
  "com.softwaremill.macwire" %% "macros" % "2.1.0" %"provided",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.14",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.11",
  "com.softwaremill.macwire" %% "macros" % "2.1.0",
  "com.iheart" %% "ficus" % "1.1.3"
)

fork in run := true
