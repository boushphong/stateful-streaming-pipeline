ThisBuild / version := "0.1.0-SNAPSHOT"
//
ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "Streaming"
  )

val flinkVersion = "1.14.4"
val log4j = "2.18.0"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-clients" % flinkVersion,
  "org.apache.flink" %% "flink-scala" % flinkVersion,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
  "org.apache.flink" %% "flink-connector-kafka" % flinkVersion,
  "org.apache.flink" %% "flink-walkthrough-common" % flinkVersion
)

val logging = Seq(
  "org.apache.logging.log4j" % "log4j-api" % log4j,
  "org.apache.logging.log4j" % "log4j-core" % log4j
)

libraryDependencies ++= flinkDependencies ++ logging
