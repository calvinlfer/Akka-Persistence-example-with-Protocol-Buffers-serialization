package com.experiments.basket.models.cereal

import com.experiments.basket.models.BasketModels
import spray.json._

// Define conversions from Scala <-> JSON here for the purpose of serialization
object JsonFormats extends DefaultJsonProtocol {
  import BasketModels._
  // Define how to convert case classes to and from JSON by default (jsonFormat3 means the case class has 3 fields)
  implicit val itemFormat: RootJsonFormat[Item] = jsonFormat3(Item)

  implicit val itemsFormat: RootJsonFormat[Items] = jsonFormat1(Items)

  implicit val addedEventFormat: RootJsonFormat[Added] = jsonFormat1(Added)

  implicit val removedEventFormat: RootJsonFormat[ItemRemoved] = jsonFormat1(ItemRemoved)

  implicit val updatedEventFormat: RootJsonFormat[ItemUpdated] = jsonFormat2(ItemUpdated)

  implicit val replacedEventFormat: RootJsonFormat[Replaced] = jsonFormat1(Replaced)

  implicit val clearedEventFormat: RootJsonFormat[Cleared] = jsonFormat0(Cleared)

  // Also define one for Snapshots (not just Events)
  implicit val snapshotFormat: RootJsonFormat[BasketSnapshot] = jsonFormat1(BasketSnapshot)

  // Specify how to convert to a generic Event from Scala <-> JSON
  // This makes use of the above specific-event formatters
  implicit object BasketEventFormat extends RootJsonFormat[Event] {
    val addedId = JsNumber(1)
    val removedId = JsNumber(2)
    val updatedId = JsNumber(3)
    val replacedId = JsNumber(4)
    val clearedId = JsNumber(5)

    // JSON -> Scala
    override def read(json: JsValue): Event = {
      json match {
        // Encountered an Added Basket Event
        case JsArray(Vector(`addedId`, jsEvent)) => addedEventFormat.read(jsEvent)
        case JsArray(Vector(`removedId`, jsEvent)) => removedEventFormat.read(jsEvent)
        case JsArray(Vector(`updatedId`, jsEvent)) => updatedEventFormat.read(jsEvent)
        case JsArray(Vector(`replacedId`, jsEvent)) => replacedEventFormat.read(jsEvent)
        case JsArray(Vector(`clearedId`, jsEvent)) => clearedEventFormat.read(jsEvent)
        case other => deserializationError(s"Expected a Basket Event, but got $other")
      }
    }

    // Scala -> JSON
    // Note that this ends up persisting a list every time it is called (a list of two elements)
    override def write(event: Event): JsValue = {
      // We need to be able to differentiate between the events we persist so we put then an JsArray
      // and use ids to differentiate
      event match {
        // Push this into a JSON array so we encode the added ID event type as the first element
        // and the actual class as the second element. Note we use Added's jsonFormat
        case e: Added => JsArray(Vector(`addedId`, addedEventFormat.write(e)))
        case e: ItemRemoved => JsArray(Vector(`removedId`, removedEventFormat.write(e)))
        case e: ItemUpdated => JsArray(Vector(`addedId`, updatedEventFormat.write(e)))
        case e: Replaced => JsArray(Vector(`addedId`, replacedEventFormat.write(e)))
        case e: Cleared => JsArray(Vector(`addedId`, clearedEventFormat.write(e)))
      }
    }
  }
}
