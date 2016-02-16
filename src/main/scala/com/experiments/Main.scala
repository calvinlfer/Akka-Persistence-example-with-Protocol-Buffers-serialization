package com.experiments

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("calculator-actor-system", config)
  val persistentCalculatorActor = system.actorOf(Props[Calculator])
}


