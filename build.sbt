name := "akka-fsms"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion       = "2.4.1"
  val sprayVersion      = "1.3.3"
  Seq(
    "com.typesafe.akka"       %% "akka-actor"       % akkaVersion,
    "com.typesafe.akka"       %% "akka-testkit"     % akkaVersion   % "test",
    "com.typesafe.akka"       %% "akka-slf4j"       % akkaVersion,
    "ch.qos.logback"          %  "logback-classic"  % "1.1.3",
    "org.scalatest"           %% "scalatest"        % "2.2.6"       % "test"
  )
}