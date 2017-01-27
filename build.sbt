import com.trueaccord.scalapb.compiler.Version.scalapbVersion

name := "akka-persistence-example-with-protocol-buffers"
version := "1.1"
scalaVersion := "2.12.1"

// Disable parallel execution of tests
parallelExecution in Test := false

// Fork tests in case native LevelDB database is used
fork := true

libraryDependencies ++= {
  val akkaVersion       = "2.4.16"
  Seq(
    // Akka
    "com.typesafe.akka"           %% "akka-actor"                           % akkaVersion,
    "com.typesafe.akka"           %% "akka-persistence"                     % akkaVersion,
    "com.typesafe.akka"           %% "akka-testkit"                         % akkaVersion   % "test",
    "com.typesafe.akka"           %% "akka-slf4j"                           % akkaVersion,
    "com.typesafe.akka"           %% "akka-persistence-query-experimental"  % akkaVersion,

    // Local LevelDB journal (Akka Persistence)
    // http://doc.akka.io/docs/akka/current/scala/persistence.html#Local_LevelDB_journal
    "org.iq80.leveldb"            % "leveldb"           % "0.9",
    "org.fusesource.leveldbjni"   % "leveldbjni-all"    % "1.8",

    // Commons IO is needed for cleaning up data when testing persistent actors
    "commons-io"                  %  "commons-io"       % "2.4",
    "ch.qos.logback"              %  "logback-classic"  % "1.1.3",
    "org.scalatest"               %% "scalatest"        % "3.0.1"         % "test",

    // allows ScalaPB proto customizations (scalapb/scalapb.proto)
    "com.trueaccord.scalapb"      %% "scalapb-runtime"  % scalapbVersion  % "protobuf"
  )
}

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
