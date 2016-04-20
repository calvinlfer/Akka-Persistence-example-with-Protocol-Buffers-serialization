package com.experiments.calculator

import akka.actor.{ActorSystem, Props}
import com.experiments.calculator.models.Models.{Add, Clear, Multiply, PrintResult}

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

  // Wait for 2 seconds before terminating
  // Just an example, you wouldn't do this normally
  Thread sleep 2000
  system terminate
}
