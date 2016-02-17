package com.experiments.basket.models

import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created on 2016-02-16.
  */
class ItemSpec extends WordSpecLike with MustMatchers {

  "The Items case class" must {
    import BasketModels._

    val akkaInActionItem = Item(productId = "Akka in Action", amount = 1, unitPrice = 60)
    val sbtInActionItem = akkaInActionItem.copy(productId = "SBT in Action", unitPrice = 45)

    "be able to add a new item to the list provided it is not already there" in {
      val emptyItems = Items(Nil)
      val singleItemInItems = emptyItems.add(akkaInActionItem)
      singleItemInItems.listOfItems.size mustEqual 1
    }

    "be able to add an item that already exists and have its amount amalgamated" in {
      val singleItemInItems = Items(akkaInActionItem :: Nil)
      val amalgamated = singleItemInItems.add(akkaInActionItem)
      amalgamated.listOfItems.size mustEqual 1
      amalgamated.listOfItems.head.amount mustEqual 2
    }

    "be able to add two items that are different" in {
      val singleItemInItems = Items(akkaInActionItem :: Nil)
      val twoItems = singleItemInItems.add(sbtInActionItem)
      twoItems.listOfItems.size mustEqual 2
      twoItems.listOfItems.find(_.productId == "Akka in Action") mustBe Some(akkaInActionItem)
      twoItems.listOfItems.find(_.productId == "SBT in Action") mustBe Some(sbtInActionItem)
    }

    "be able to add two item lists and take care of amalgamation" in {
      val amalgamatedItems =
        Items(akkaInActionItem :: sbtInActionItem :: Nil).add(Items(akkaInActionItem :: sbtInActionItem :: Nil))
      amalgamatedItems.listOfItems.size mustEqual 2
      amalgamatedItems.listOfItems.find(_.productId == "Akka in Action") mustBe Some(akkaInActionItem.copy(amount = 2))
      amalgamatedItems.listOfItems.find(_.productId == "SBT in Action") mustBe Some(sbtInActionItem.copy(amount = 2))
    }

    "be able to update an item in an Item list" in {
      val manyItems =
        Items(akkaInActionItem :: sbtInActionItem :: Nil).add(Items(akkaInActionItem :: sbtInActionItem :: Nil))
      val updated = manyItems.updateItem(productId = "Akka in Action", amount = 10)
      // Items do not change but rather a single item's amount does
      updated.listOfItems.size mustBe manyItems.listOfItems.size
      updated.listOfItems.find(_.productId == "Akka in Action") mustBe Some(akkaInActionItem.copy(amount = 10))
    }

    "be able to clear a basket full of items" in {
      val manyItems = Items(akkaInActionItem :: sbtInActionItem :: Nil)
      manyItems.clear.listOfItems mustBe empty
    }
  }
}
