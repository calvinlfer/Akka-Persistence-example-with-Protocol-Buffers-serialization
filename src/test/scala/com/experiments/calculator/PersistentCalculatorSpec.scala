package com.experiments.calculator

import akka.actor.{ActorSystem, Props}
import com.experiments.PersistenceSpec
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  * Note that we use the PersistenceSpec we defined instead of the usual TestKit which takes care of cleaning up the
  * left over files from previous tests
  */
class PersistentCalculatorSpec extends PersistenceSpec(ActorSystem("actor-test-system"))
  with WordSpecLike
  with BeforeAndAfterAll {
  "The Calculator" should {
    import com.experiments.calculator.models.Models._
    "recover last known result after crash" in {
      val calc = system.actorOf(Props[Calculator], "test-calculator")
      calc ! Add(1)
      calc ! GetResult
      // Note that PersistenceSpec mixes in ImplicitSender which is why the test itself is an actor
      expectMsg(1)

      calc ! Subtract(0.5)
      calc ! GetResult
      expectMsg(0.5)

      killActors(calc)

      val calcResurrected = system.actorOf(Props[Calculator], "test-calculator")
      calcResurrected ! GetResult
      expectMsg(0.5)

      calcResurrected ! Add(1)
      calcResurrected ! GetResult
      expectMsg(1.5)
    }
  }
}
