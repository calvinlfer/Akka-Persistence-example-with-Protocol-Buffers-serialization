logLevel := Level.Warn

// Informative Scala compiler errors
addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt" % "0.4.1")

// Scala Protocol Buffers Compiler
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.3")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.5.47"
