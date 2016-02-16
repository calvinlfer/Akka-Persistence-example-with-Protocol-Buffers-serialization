package com.experiments.calculator

import akka.actor.{ActorSystem, Props}
import com.experiments.calculator.models.Models._

import scala.language.postfixOps

/**
  * Created on 2016-02-16.
  */
object Main extends App {
  val system = ActorSystem("calculator-actor-system")
  val persistentCalculatorActor = system.actorOf(Props[Calculator])

  // Send messages
  persistentCalculatorActor ! PrintResult
  persistentCalculatorActor ! Add(2)
  persistentCalculatorActor ! Multiply(2)
  persistentCalculatorActor ! PrintResult

  // Wait for 5 seconds before terminating
  Thread sleep 5000
  system terminate
}
