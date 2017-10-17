name := "HearthSandBox"

trapExit := false

version := "1.0"

scalaVersion := "2.12.3"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

val akkaVersion = "2.5.4"
val pureconfigVersion = "0.8.0"
val parquetVersion = "1.8.1"
val hadoopVersion = "2.8.1"
val quillVersion = "1.4.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,
  "com.github.pureconfig" %% "pureconfig-akka" % pureconfigVersion
)

libraryDependencies ++= Seq(
  "org.apache.parquet" % "parquet-avro" % parquetVersion
)

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion
)

libraryDependencies ++= Seq(
  "io.getquill" %% "quill-cassandra" % quillVersion
)