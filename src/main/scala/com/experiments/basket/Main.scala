package com.experiments.basket

import akka.actor.{ ActorSystem, Props }
import com.experiments.basket.models.BasketModels._

import scala.language.postfixOps

/**
 * Created on 2016-02-16.
 */
object Main extends App {
  val system = ActorSystem("basket-actor-system")
  val persistentBasketActor = system.actorOf(Props[BasketActor])

  val shopperId = 2L
  val macbookPro = Item("Apple Macbook Pro", 1, BigDecimal(2499.99))
  val macPro = Item("Apple Mac Pro", 1, BigDecimal(10499.99))
  val displays = Item("4K Display", 3, BigDecimal(2499.99))
  val appleMouse = Item("Apple Mouse", 1, BigDecimal(99.99))
  val appleKeyboard = Item("Apple Keyboard", 1, BigDecimal(79.99))
  val dWave = Item("D-Wave One", 1, BigDecimal(14999999.99))

  // Send messages
  println("Starting Now...")
  persistentBasketActor ! PrintItems(shopperId)
  persistentBasketActor ! AddItem(macbookPro, shopperId)
  persistentBasketActor ! AddItem(displays, shopperId)
  persistentBasketActor ! PrintItems(shopperId)
  persistentBasketActor ! Clear(shopperId)
  persistentBasketActor ! PrintItems(shopperId)
  persistentBasketActor ! AddItem(macbookPro, shopperId)
  persistentBasketActor ! AddItem(displays, shopperId)
  persistentBasketActor ! PrintItems(shopperId)

  // Wait for 2 second before terminating
  Thread sleep 2000
  system terminate
}
