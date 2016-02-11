package com.experiments

import akka.actor.FSM.State
import akka.actor.{FSM, Actor}
import com.experiments.models.Models.{StateData, WaitForRequests}

class Inventory extends Actor with FSM[State, StateData] {
  // Define the initial state of our FSM
  // initialize our StateData to have no books in the store and 0 pending state requests
  startWith(WaitForRequests, StateData(0, Seq()))


}
