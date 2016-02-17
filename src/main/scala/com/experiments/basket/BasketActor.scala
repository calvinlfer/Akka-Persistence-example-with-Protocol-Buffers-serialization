package com.experiments.basket

import akka.actor.ActorLogging
import akka.persistence.{SaveSnapshotSuccess, SaveSnapshotFailure, SnapshotOffer, PersistentActor}

/**
  * This is a persistent actor responsible for holding a shopping cart basket
  */
class BasketActor extends PersistentActor with ActorLogging {
  import models.BasketModels._

  var itemsInBasket = Items(Nil)
  var nrOfEventsRecovered = 0

  override def persistenceId: String = "sample-persistent-basket-actor"

  // General updateState method for re-use
  // Used by both receiveRecover and receiveCommand
  // This updates the internal state using an event
  val updateState: (Event => Unit) = {
    case Added(item) => itemsInBasket = itemsInBasket.add(item)
    case ItemRemoved(productId) => itemsInBasket = itemsInBasket.removeItem(productId)
    case ItemUpdated(id, amount) => itemsInBasket = itemsInBasket.updateItem(id, amount)
    case Replaced(newItems) => itemsInBasket = newItems
    case Cleared => itemsInBasket = itemsInBasket.clear
  }

  // Events come here (recovery phase) from database (snapshot and event)
  override def receiveRecover: Receive = {
    // recovering from event journal database
    case event: Event =>
      log.info(s"Recovering events from event journal, persistence ID: $persistenceId")
      log.info(s"Event: $event")
      nrOfEventsRecovered = nrOfEventsRecovered + 1
      updateState(event)

    // recovering from snapshot journal database
    case SnapshotOffer(metadata, basketSnapshot: BasketSnapshot) =>
      log.info(s"Recovering basket from snapshot, persistence ID: $persistenceId")
      log.info(s"Metadata: $metadata")
      log.info(s"Snapshot Data: $basketSnapshot")
      itemsInBasket = basketSnapshot.items
  }

  // Commands come here (active phase)
  override def receiveCommand: Receive = {
    // Turn Command into Event, persist it then update the internal state
    case AddItem(item, _) => persist(Added(item))(updateState)

    case RemoveItem(productId, _) =>
      if (itemsInBasket.containsProduct(productId)) {
        persist(ItemRemoved(productId)) {
          itemRemovedEvent =>
            updateState(itemRemovedEvent)
            sender() ! Some(itemRemovedEvent)
        }
      } else sender() ! None

    case UpdateItem(productId, amount, _) =>
      if (itemsInBasket.containsProduct(productId)) {
        persist(ItemUpdated(productId, amount)) {
          itemUpdatedEvent =>
            updateState(itemUpdatedEvent)
            sender() ! Some(itemUpdatedEvent)
        }
      } else sender() ! None

    case Replace(items, _) => persist(Replaced(items))(updateState)

    // Save snapshot on basket clear
    // saveSnapshot will cause SaveSnapshotFailure or SaveSnapshotSuccess messages to be sent to the current actor
    case Clear(_) => persist(Cleared) {
      clearedEvent =>
        updateState(clearedEvent)
        saveSnapshot(BasketSnapshot(itemsInBasket))
    }

    case GetItems(_) => sender() ! itemsInBasket

    case CountRecoveredEvents(_) => sender() ! RecoveredEventsCount(nrOfEventsRecovered)

    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Snapshot failed to save:")
      log.error(s"Metadata: $metadata")
      log.error(s"Reason: $reason")

    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Snapshot saved successfully")
      log.info(s"Metadata: $metadata")
  }
}
