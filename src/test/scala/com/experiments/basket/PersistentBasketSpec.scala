package com.experiments.basket

import akka.actor.{Props, ActorSystem}
import com.experiments.PersistenceSpec

/**
  * The Basket Specification is responsible for testing the Basket Actor which is persistent and uses
  * snapshots and events to recover quickly
  */
class PersistentBasketSpec extends PersistenceSpec(ActorSystem("basket-actor-test-system")) {
  import models.BasketModels._

  val shopperId = 2L
  val macbookPro = Item("Apple Macbook Pro", 1, BigDecimal(2499.99))
  val macPro = Item("Apple Mac Pro", 1, BigDecimal(10499.99))
  val displays = Item("4K Display", 3, BigDecimal(2499.99))
  val appleMouse = Item("Apple Mouse", 1, BigDecimal(99.99))
  val appleKeyboard = Item("Apple Keyboard", 1, BigDecimal(79.99))
  val dWave = Item("D-Wave One", 1, BigDecimal(14999999.99))

  "The basket" should {
    "skip basket events that occurred before Cleared during recovery" in {
      val basket = system.actorOf(Props[BasketActor], "shopper-basket")
      basket ! AddItem(macbookPro, shopperId)
      basket ! AddItem(displays, shopperId)
      basket ! GetItems(shopperId)
      expectMsg(Items(List(macbookPro, displays)))
      basket ! Clear(shopperId)

      basket ! AddItem(macPro, shopperId)
      basket ! RemoveItem(macPro.productId, shopperId)
      expectMsg(Some(ItemRemoved(macPro.productId)))

      basket ! Clear(shopperId)
      // at this point, the latest snapshot is created so when recovering, snapshot information should be replayed
      // from here along with events that take place below
      basket ! AddItem(dWave, shopperId)
      basket ! AddItem(displays, shopperId)

      basket ! GetItems(shopperId)
      expectMsg(Items(List(dWave, displays)))

      killActors(basket)

      val basketResurrected = system.actorOf(Props[BasketActor], "shopper-basket")
      basketResurrected ! GetItems(shopperId)
      expectMsg(Items(List(dWave, displays)))

      // Only 2 events (that need to be persisted to the event journal) are present after the snapshot
      // This is because Akka will start from the latest snapshot and read events after that snapshot
      basketResurrected ! CountRecoveredEvents(shopperId)
      expectMsg(RecoveredEventsCount(2))

      killActors(basketResurrected)
    }
  }
}
