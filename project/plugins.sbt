logLevel := Level.Warn

// Informative Scala compiler errors
addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt" % "0.3.0")

// Scala Protocol Buffers Compiler
addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.32")

// Protoc-jar so we don't need the Protoc compiler
libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b3"