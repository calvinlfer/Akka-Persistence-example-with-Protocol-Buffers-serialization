package com.experiments.models

object Models {

  // FSM states
  sealed trait State

  case object WaitForRequests extends State

  case object ProcessRequest extends State

  case object WaitForPublisher extends State

  case object SoldOut extends State

  case object ProcessSoldOut extends State


  // This is the data that we use when we need a state condition to decide which transition is fired,
  // so it contains all the pending requests and the number of books in store.
  case class StateData(nrOfBooksInStore: Int, pendingRequests: Seq[BookRequest])

}

