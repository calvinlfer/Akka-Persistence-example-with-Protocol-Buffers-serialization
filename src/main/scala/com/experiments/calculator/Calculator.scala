package com.experiments.calculator

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import com.experiments.calculator.models.Models._


/**
  * The persistent event-sourced calculator actor
  * The calculator actor receives commands, converts them
  * to events and persists them before dealing with them
  */
class Calculator extends PersistentActor with ActorLogging {

  // Persistence ID must be unique for persistent actors as Akka Persistence uses this ID to recover state
  override def persistenceId: String = "calculator-persistent-actor"

  // Internal state of the actor
  var state = CalculationResult(result = 0)

  // General updateState method for re-use
  // Used by both receiveRecover and receiveCommand
  // This updates the internal state using an event
  val updateState: Any => Unit = {
    case Reset(_) => state = state.reset
    case Added(value) => state = state.add(value)
    case Subtracted(value) => state = state.subtract(value)
    case Divided(value) => state = state.divide(value)
    case Multiplied(value) => state = state.multiply(value)
  }

  // Events come here (recovery phase) from database (snapshot and event)
  override def receiveRecover: Receive = {
    // comes from the snapshot journal
    case SnapshotOffer(metadata, Reset(_)) => updateState(Reset(-1))

    // this message is sent once recovery has completed
    case RecoveryCompleted => log.info(s"Recovery has completed for $persistenceId")

    // all other events come from the event database journal
    case event: Any => updateState(event)
  }

  // Commands come here (active phase)
  override def receiveCommand: Receive = {
    // Add this number to the result
    case Add(value) =>
      // Convert Command to Event
      // We generate an Added Event (as in `I have added this`)
      val event = Added(value)
      // Persist the event to the database and then call update state with the persisted event
      persist(event)(updateState)

    case Subtract(value) => persist(Subtracted(value))(updateState)

    // We see validation in the case of division
    case Divide(value) =>
      if (value > 0) persist(Divided(value))(updateState)
      else log.error("Cannot divide by 0, Ignoring command")

    case Multiply(value) => persist(Multiplied(value))(updateState)

    case PrintResult => println(s"the result is: ${state.result}")

    case GetResult => sender() ! state.result

    case Clear =>
      persist(Reset(-1))(updateState)
      // tell the snapshot database to persist a Reset event, we would usually tell it to persist our internal state
      // but I'm lazy and I don't want to make our internal state (CalculationResult) a part of the proto because then
      // I'd have to serialize that
      saveSnapshot(Reset(-1))
  }
}
