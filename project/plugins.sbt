logLevel := Level.Warn

// Informative Scala compiler errors
addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt" % "0.2.5")

// sbt plugin for generating Scala case classes from Apache Avro schemas, datafiles, and protocols.
addSbtPlugin("com.julianpeeters" % "sbt-avrohugger" % "0.9.6")