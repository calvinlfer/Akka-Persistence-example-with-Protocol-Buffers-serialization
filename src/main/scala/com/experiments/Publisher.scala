package com.experiments

import akka.actor.Actor
import scala.math._

/**
  * The Publisher for the Inventory Actor FSM
  */
class Publisher(totalNrOfBooks: Int, nrOfBooksPerRequest: Int) extends Actor {
  import com.experiments.models.Models._

  var nrLeft = totalNrOfBooks
  def receive = {
    case PublisherRequest =>
      if (nrLeft == 0) sender() ! BookSupplySoldOut
      else {
        val supply = min(nrOfBooksPerRequest, nrLeft)
        nrLeft -= supply
        // Send them the books
        sender() ! BookSupply(supply)
      }
  }
}
