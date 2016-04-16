# Akka Persistence example with Protocol Buffers serialization

This example uses [ScalaPB](https://trueaccord.github.io/ScalaPB/) in order to obtain case class support for Scala when
generating code from `*.proto` files. This project also makes use of [protoc-jar](https://github.com/os72/protoc-jar) in
order to keep a self-contained project without posing any requirements on the user of having the Protocol Buffer
compiler on their system.

Execute `sbt run` in order to run the `Main` application. The `Calculator` actor is used to persist events across runs
using Event Sourcing. We define a custom serializer that makes use of the generated class' (from proto)
serialization mechanisms to easily serialize and deserialize events.

Unfortunately, you cannot compile through IntelliJ so `sbt compile` will have to do.

The `Main` application fires events at the `Calculator` actor. Since `Calculator` actor is Persistent, it makes use of
the Serializer (set up in `application.conf`) when reading and persistent events to/from the event journal.

We also have a [test](https://github.com/referentiallytransparent/Akka-Persistence-example-with-Protocol-Buffers-serialization/blob/master/src/test/scala/com/experiments/calculator/PersistentCalculatorSpec.scala) that exercises the Persistent `Calculator` Actor by sending it events, killing it and making sure it uses Event Sourcing to bring the `Calculator` up to the current state. You can use `sbt test` to run the test.
 
 If you are using IntelliJ and want syntax highlighting, then make sure your directory setup looks like this:
 ![image](https://cloud.githubusercontent.com/assets/14280155/14578746/e1a8e258-035e-11e6-86af-5a74669930d5.png)
