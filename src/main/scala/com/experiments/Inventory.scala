package com.experiments

import akka.actor.{ActorLogging, Actor, FSM}
import com.experiments.models.Models._

/**
  * This is an Actor FSM
  * Events come in one at a time and state transitions occur
  *
  * The concurrency is in the fact that if a BookRequest comes in whilst the FSM is in a
  * busy state, it will just queue the BookRequest (in its StateData) and continue its work. This part
  * is shown in the whenUnhandled block which is triggered when BookRequests are sent in busy states or
  * states where the request cannot be processed right away
  */
class Inventory extends Actor with FSM[FsmState, StateData] with ActorLogging {
  // Define the initial state of our FSM
  // initialize our StateData to have no books in the store and 0 pending state requests
  startWith(WaitForRequests, StateData(0, Seq[BookRequest]()))

  // Declare the transitions for FSM state WaitForRequests
  // So whilst you are in this FSM state, we define how to react to Events and perform state transitions
  when(WaitForRequests) {
    // A BookRequest event may occur in the WaitForRequests state
    case Event(request: BookRequest, data: StateData) =>
      log.info("Received a BookRequest event in state {} where StateData: {}", stateName, stateData)
      // append a new request to the list of pending requests
      val newStateData = data.copy(pendingRequests = data.pendingRequests :+ request)
      if (newStateData.nrOfBooksInStore > 0) {
        // The receiving of this Event causes a transition to the ProcessRequest state with the
        // update StateData
        goto(ProcessRequest) using newStateData
      } else {
        // The receiving of this Event causes a transition to the WaitForPublisher state with the
        // update StateData
        goto(WaitForPublisher) using newStateData
      }

    // A PendingRequests event may occur in the WaitForRequests state
    case Event(PendingRequests, data: StateData) =>
      log.info("Received a PendingRequests event in state {} where StateDate: {}", stateName, stateData)
      // Note that here, we do stay() and goto(...) without providing the updated StateData
      // This means we want the StateData to remain the same without changing
      // If there are no pending requests then stay in the current state and do not transition
      if (data.pendingRequests.isEmpty) stay()
      else if (data.nrOfBooksInStore > 0) goto(ProcessRequest)
      else goto(WaitForPublisher)
  }

  // Declare the transitions for FSM state WaitForPublisher
  when(WaitForPublisher) {
    case Event(supply:BookSupply, data:StateData) =>
      log.info("Received a BookSupply event in state {} where StateDate: {}", stateName, stateData)
      goto(ProcessRequest) using data.copy(nrOfBooksInStore = supply.nrBooks)

    case Event(BookSupplySoldOut, _) =>
      log.info("Received a BookSupplySoldOut event in state {} where StateDate: {}", stateName, stateData)
      goto(ProcessSoldOut)
  }

  // Declare the transitions for FSM state ProcessRequest
  when(ProcessRequest) {
    case Event(Done, data:StateData) =>
      log.info("Received a Done event in state {} where StateDate: {}", stateName, stateData)
      goto(WaitForRequests) using data.copy(
        nrOfBooksInStore = data.nrOfBooksInStore - 1,
        pendingRequests =  data.pendingRequests.tail)
  }

  // Declare the transitions for FSM state SoldOut
  when(SoldOut) {
    case Event(request:BookRequest, data:StateData) =>
      log.info("Received a BookRequest event in state {} where StateDate: {}", stateName, stateData)
      goto(ProcessSoldOut) using StateData(0, Seq(request))
  }

  // Declare the transitions for FSM state ProcessSoldOut
  when(ProcessSoldOut) {
    case Event(Done, data:StateData) =>
      log.info("Received a Done event in state {} where StateDate: {}", stateName, stateData)
      goto(SoldOut) using StateData(0,Seq())
  }

  whenUnhandled {
    // A BookRequest event may occur in the many different FSM states so it is placed in the whenUnhandled block
    // for convenience sakes so we don't have to write out how to handle this event in each FSM State when block
    // if the FSM state's when block handles it then it won't come here (for example, the WaitForRequests' state
    // handles this type of event in a separate way)
    // In this case, a BookRequest comes in a busy working state, so the BookRequest is just queued so it can be
    // worked on later
    case Event(request:BookRequest, data:StateData) =>
      log.info("Received a BookRequest event in state {} where StateDate: {}", stateName, stateData)
      val newStateData = data.copy(pendingRequests =  data.pendingRequests :+ request)
      stay using newStateData


    // The incoming event couldn't be handled by the state and so it comes here for logging
    case Event(event, stateData) =>
      // note stateName is part of the actor and tells us in the FSM state we are currently in
      log.info("Received an unhandled request {} in state {} where StateData: {}", event, stateName, stateData)
      stay()
  }
}
