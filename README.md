# Akka Persistence example with Protobuf serialization

Execute `sbt run` in order to run the `Main` application and see the Calculator being used and persisting events across
runs using the Event Sourcing technique. We define a custom serializer that makes use of the generated class'
(from proto) serialization mechanisms to easily serialize and deserialize events.

Unfortunately, you cannot compile through IntelliJ so `sbt compile` will have to do

The `Main` application fires events at the `Calculator` actor. Since Calculator actor is Persistent, it makes use of the
 Serializer (set up in `application.conf`) when reading and persistent events to/from the event journal