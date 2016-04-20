# Akka Persistence example with Avro serialization

This example uses AvroHugger and Avro4S in order to obtain case class support for Scala when
generating code from Avro based files.

Execute `sbt run` in order to run the `Main` application. The `Calculator` actor is used to persist events across runs
using Event Sourcing. We define a custom serializer that makes use of the generated class' (from proto)
serialization mechanisms to easily serialize and deserialize events.

Unfortunately, you cannot compile through IntelliJ so `sbt compile` will have to do.

The `Main` application fires events at the `Calculator` actor. Since `Calculator` actor is Persistent, it makes use of
the Serializer (set up in `application.conf`) when reading and persistent events to/from the event journal.