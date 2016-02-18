# README #

This covers the workings of an [event-sourced](http://doc.akka.io/docs/akka/snapshot/scala/persistence.html#Event_sourcing) Persistent Actor and how the event journal and snapshot journal can be used to recover state when a persistent actor crashes or starts up along with switching to a custom serializer (JSON serialization) with the help of Spray's converters. So events and snapshots are not using the default Java serialization mechanism anymore resulting in a speed-up.
