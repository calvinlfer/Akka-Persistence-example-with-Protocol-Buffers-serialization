package com.experiments.basket.models.cereal

import akka.serialization.Serializer
import com.experiments.basket.models.BasketModels
import spray.json._

/**
  * Define a serializer-deserializer for persisting Basket Snapshots for Akka's snapshot journal
  */
class BasketSnapshotSerializer extends Serializer {
  import BasketModels._
  import JsonFormats._

  // Completely unique value to identify this implementation of Serializer, used to optimize network traffic.
  // Values from 0 to 16 are reserved for Akka internal usage.
  // Make sure this does not conflict with any other kind of serializer or you will have problems
  override def identifier: Int = 90010001

  // This implementation of the serializer does not need a manifest
  override def includeManifest: Boolean = false

  // JSON -> Scala
  // I have tightened the return type from AnyRef to Snapshot
  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): BasketSnapshot = {
    val jsonAst = new String(bytes).parseJson
    snapshotFormat.read(jsonAst)
  }

  override def toBinary(scalaObject: AnyRef): Array[Byte] = {
    scalaObject match {
      case snap: BasketSnapshot => snapshotFormat.write(snap).compactPrint.getBytes
      case other => serializationError(s"Cannot serialize Basket Snapshot: $other with $getClass")
    }
  }
}
