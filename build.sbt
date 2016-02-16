name := "akka-persistence"

version := "1.0"

scalaVersion := "2.11.7"

// Disable parallel execution of tests
parallelExecution in Test := false

// Fork tests in case native LevelDB database is used
fork := true

libraryDependencies ++= {
  val akkaVersion       = "2.4.1"
  val sprayVersion      = "1.3.3"
  Seq(
    // Akka
    "com.typesafe.akka"           %% "akka-actor"       % akkaVersion,
    "com.typesafe.akka"           %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka"           %% "akka-testkit"     % akkaVersion   % "test",
    "com.typesafe.akka"           %% "akka-slf4j"       % akkaVersion,
    // Local journal (Akka Persistence)
    // http://doc.akka.io/docs/akka/2.4.1/scala/persistence.html#Local_LevelDB_journal
    "org.iq80.leveldb"            % "leveldb"          % "0.7",
    "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
    "ch.qos.logback"              %  "logback-classic"  % "1.1.3",
    "org.scalatest"               %% "scalatest"        % "2.2.6"       % "test"
  )
}