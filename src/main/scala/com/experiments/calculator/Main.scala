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

  /*
  Simple example of using the built in serializer
  println {
    "The result is going to be!!!! " +
    // deserialize
    Multiplied.parseFrom {
      // serialize
      Multiplied(1).toByteArray
    }
  }
  */

  // Wait for 1 second before terminating
  Thread sleep 1000
  system terminate
}
