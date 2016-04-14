# Akka Persistence example with custom JSON serialization (Spray JSON) #

This covers the workings of an [event-sourced](http://doc.akka.io/docs/akka/snapshot/scala/persistence.html#Event_sourcing) Persistent Actor and how the event journal and snapshot journal can be used to recover state when a persistent actor crashes or starts up along with switching Basket Events and Basket Snapshots to a custom serializer (JSON serialization) with the help of Spray's converters. So events and snapshots are not using the default Java serialization mechanism anymore resulting in a speed-up.

We cover two separate examples:

- [Calculator](https://github.com/referentiallytransparent/Akka-persistence-example-with-custom-serializer/tree/master/src/main/scala/com/experiments/calculator)
- [Basket](https://github.com/referentiallytransparent/Akka-persistence-example-with-custom-serializer/tree/master/src/main/scala/com/experiments/basket)

Calculator is meant to be a simple example whereas Basket is slightly more complex as it makes use of a custom serializer. The event journal and snapshot store is provided by LevelDB

