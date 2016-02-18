package com.experiments.basket.models.cereal

import akka.serialization.Serializer
import com.experiments.basket.models.BasketModels
import spray.json._

/**
  * Define a serializer-deserializer for persisting Basket Events for Akka's event journal
  */
class BasketEventSerializer extends Serializer {
  import BasketModels._
  import JsonFormats._
  // Completely unique value to identify this implementation of Serializer, used to optimize network traffic.
  // Values from 0 to 16 are reserved for Akka internal usage.
  // Make sure this does not conflict with any other kind of serializer or you will have problems
  override def identifier: Int = 90020001

  // This implementation of the serializer does not need a manifest
  override def includeManifest: Boolean = false

  // JSON -> Scala
  // I have tightened the return type from AnyRef to Event
  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): Event = {
    val jsonAst = new String(bytes).parseJson
    BasketEventFormat.read(jsonAst)
  }

  override def toBinary(scalaObject: AnyRef): Array[Byte] = {
    scalaObject match {
      case e: Event => BasketEventFormat.write(e).compactPrint.getBytes
      case other => serializationError(s"Cannot serialize Basket Event: $other with $getClass")
    }
  }
}


