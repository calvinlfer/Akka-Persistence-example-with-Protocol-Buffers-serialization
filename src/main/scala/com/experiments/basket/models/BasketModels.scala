package com.experiments.basket.models

/**
 * Created on 2016-02-16.
 */
object BasketModels {

  // Commands: Do this action (potentially harmful)
  sealed trait Command {val shopperId: Long}
  case class AddItem(item: Item, shopperId: Long) extends Command
  case class RemoveItem(productId: String, shopperId: Long) extends Command
  case class UpdateItem(productId: String, amount: Int, shopperId: Long) extends Command
  case class Clear(shopperId: Long) extends Command
  case class Replace(items: Items, shopperId: Long) extends Command

  // Commands that do not generate events
  case class GetItems(shopperId: Long) extends Command
  case class CountRecoveredEvents(shopperId: Long) extends Command
  case class PrintItems(shopperId: Long) extends Command

  // Events: I have done this action (not harmful)
  sealed trait Event extends Serializable
  case class Added(item: Item) extends Event
  case class ItemRemoved(productId: String) extends Event
  case class ItemUpdated(productId: String, amount: Int) extends Event
  case class Replaced(items: Items) extends Event
  // I would have used a case object but I need this for serialization to work
  case class Cleared() extends Event

  // Responses for statistical information
  case class RecoveredEventsCount(count: Int)

  // container for snapshots
  case class BasketSnapshot(items: Items)

  // State
  case class Item(productId: String, amount: Int, unitPrice: BigDecimal)
  case class Items(listOfItems: List[Item]) {

    def containsProduct(productId: String): Boolean = listOfItems.exists(_.productId == productId)

    def removeItem(productId: String): Items = copy(listOfItems.filterNot(_.productId == productId))

    def updateItem(productId: String, amount: Int): Items = {
      // Find the item if present in the list
      val potentialItem = listOfItems.find(_.productId == productId)
      potentialItem.fold {
        // Item is not present
        this
      } {
        // item is present
        alreadyPresentItem =>
          // remove the old item and add the new updated item which reflects the updated amount
          copy(listOfItems.filterNot(_.productId == productId) :+ alreadyPresentItem.copy(amount = amount))
      }
    }

    def clear: Items = Items(listOfItems = Nil)

    def add(newItem: Item): Items = {
      val potentialItem: Option[Item] = listOfItems.find(_.productId == newItem.productId)
      potentialItem.fold {
        // item not present already
        copy(listOfItems = listOfItems :+ newItem)
      } {
        // item is present
        alreadyPresentItem =>
          copy(
            // remove the old item and add the new updated item which reflects the correct amount
            listOfItems = listOfItems.filterNot(eachItem => eachItem.productId == newItem.productId) :+
            alreadyPresentItem.copy(amount = alreadyPresentItem.amount + newItem.amount)
          )
      }
    }

    def add(newItems: Items): Items = newItems.listOfItems.foldLeft(this) {(acc, next) => acc.add(next)}
  }
}
