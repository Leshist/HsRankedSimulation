name := "HearthSandBox"

trapExit := false

version := "1.0"

scalaVersion := "2.12.3"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % Test
)

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.8.0"
)

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig-akka" % "0.8.0"
)
