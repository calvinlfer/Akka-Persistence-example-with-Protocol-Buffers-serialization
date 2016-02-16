package com.experiments

import akka.actor.{Props, ActorSystem}

import scala.language.postfixOps

object Main extends App {
  val system = ActorSystem("calculator-actor-system")
  val persistentCalculatorActor = system.actorOf(Props[Calculator])

  // Send messages
  import models.Models._
  persistentCalculatorActor ! PrintResult
  persistentCalculatorActor ! Add(2)
  persistentCalculatorActor ! Multiply(2)
  persistentCalculatorActor ! PrintResult

  // Wait for 5 seconds before terminating
  Thread sleep 5000
  system terminate
}


