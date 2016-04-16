package com.experiments.calculator.serialization

import akka.serialization.SerializerWithStringManifest
import com.experiments.calculator.models._

/**
  * This class is responsible for serializing Calculator Events before they make their way to the event journal
  * or snapshot journal because we don't want to use Java Serialization. Thanks to ScalaPB generating our classes from
  * protos, we can easily convert to and from the binary format that we use in the journals.
  *
  *                                                                                          ________________
  * When asked to persist an event                                                          |                |
  * Calculator Event (Multiplied(2)) -> Serializer -> Serialized form of Multiplied(2) ->   |     Journal    |
  *                                                                                         |                |
  * When asked to recover                                                                   |                |
  * Calculator Event (Multiplied(2)) <- Deserializer <- Serialized form of Multiplied(2) <- |                |
  *                                                                                         ------------------
  *
  * Based off http://doc.akka.io/docs/akka/2.4.4/scala/persistence-schema-evolution.html
  */
class CalculatorEventProtoBufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 9001

  // Event <- **Deserializer** <- Serialized(Event) <- Journal
  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case AddedManifest => Added.parseFrom(bytes)
      case SubtractedManifest => Subtracted.parseFrom(bytes)
      case MultipliedManifest => Multiplied.parseFrom(bytes)
      case DividedManifest => Divided.parseFrom(bytes)
      case ResetManifest => Reset.parseFrom(bytes)
    }

  // We use the manifest to determine the event (it is called for us during serializing)
  // Akka will call manifest and attach it to the message in the event journal/snapshot database
  // when toBinary is being invoked
  override def manifest(o: AnyRef): String = o.getClass.getName
  final val AddedManifest = classOf[Added].getName
  final val SubtractedManifest = classOf[Subtracted].getName
  final val MultipliedManifest = classOf[Multiplied].getName
  final val DividedManifest = classOf[Divided].getName
  final val ResetManifest = classOf[Reset].getName

  // Event -> **Serializer** -> Serialized(Event) -> Journal
  override def toBinary(o: AnyRef): Array[Byte] = {
    o match {
      case a: Added => a.toByteArray
      case s: Subtracted => s.toByteArray
      case m: Multiplied => m.toByteArray
      case d: Divided => d.toByteArray
      case r: Reset => r.toByteArray
    }
  }
}
