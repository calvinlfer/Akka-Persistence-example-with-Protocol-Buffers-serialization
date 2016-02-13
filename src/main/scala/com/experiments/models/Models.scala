package com.experiments.models

import akka.actor.ActorRef

object Models {

  // FSM states
  sealed trait FsmState

  case object WaitForRequests extends FsmState

  case object ProcessRequest extends FsmState

  case object WaitForPublisher extends FsmState

  case object SoldOut extends FsmState

  case object ProcessSoldOut extends FsmState

  // events
  case class BookRequest(context: AnyRef, target: ActorRef)

  case class BookSupply(nrBooks: Int)

  case object BookSupplySoldOut

  case object Done

  case object PendingRequests

  // This is the data that we use when we need a state condition to decide which transition is fired,
  // so it contains all the pending requests and the number of books in store.
  case class StateData(nrOfBooksInStore: Int, pendingRequests: Seq[BookRequest])

}

