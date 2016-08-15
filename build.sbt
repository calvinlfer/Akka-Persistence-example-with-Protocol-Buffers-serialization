import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

name := "akka-persistence"
version := "1.0"
scalaVersion := "2.11.8"

// Disable parallel execution of tests
parallelExecution in Test := false

// Fork tests in case native LevelDB database is used
fork := true

libraryDependencies ++= {
  val akkaVersion       = "2.4.9-RC2"
  Seq(
    // Akka
    "com.typesafe.akka"           %% "akka-actor"       % akkaVersion,
    "com.typesafe.akka"           %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka"           %% "akka-testkit"     % akkaVersion   % "test",
    "com.typesafe.akka"           %% "akka-slf4j"       % akkaVersion,
    "com.typesafe.akka"           %% "akka-persistence-query-experimental" % akkaVersion,

    // Local journal (Akka Persistence)
    // http://doc.akka.io/docs/akka/2.4.8/scala/persistence.html#Local_LevelDB_journal
    "org.iq80.leveldb"            % "leveldb"          % "0.9",
    "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",

    // Commons IO is needed for cleaning up data when testing persistent actors
    "commons-io"                  %  "commons-io"       % "2.4",
    "ch.qos.logback"              %  "logback-classic"  % "1.1.3",
    "org.scalatest"               %% "scalatest"        % "2.2.6"       % "test",

    // allows ScalaPB proto customizations (scalapb/scalapb.proto)
    "com.trueaccord.scalapb"      %% "scalapb-runtime"  % "0.5.34"       % PB.protobufConfig
  )
}

PB.protobufSettings
PB.runProtoc in PB.protobufConfig := {
  args => com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
}
version in PB.protobufConfig := "3.0.0-beta-3"