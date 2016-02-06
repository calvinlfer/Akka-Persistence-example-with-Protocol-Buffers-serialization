package com.experiments

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestProbe, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class RoutingSlipSpec
  extends TestKit(ActorSystem("scatter-gatherer-test-system"))
    with WordSpecLike
    with BeforeAndAfterAll {
  import RoutingSlip._

  // Make sure to shut down the actor system otherwise it will run forever
  override def afterAll(): Unit = {
    system.terminate()
  }

  "The Routing Slip Enterprise Integration pattern" must { // suite + setup
    val interestedParty = TestProbe()
    val router = system.actorOf(Props(new SlipRouter(interestedParty.ref)))
    import CarOption._

    "order a car that is painted black with no other options" in { // test
      val blackCarOrder = Order(Seq(ColorBlack))

      // send message to the dynamic slip router
      router ! blackCarOrder
      interestedParty.expectMsg(Car(color = "black"))
    }

    "order a car that is painted gray and is fully loaded" in { // test
      val fullyLoadedCar = Order(Seq(ColorGray, Navigation, ParkingSensors))
      router ! fullyLoadedCar
      interestedParty expectMsg Car(color = "gray", hasNavigation = true, hasParkingSensors = true)
    }

    "order a car that is painted black and has parking sensors" in { // test
      val blackCarWithParkingSensors = Order(Seq(ColorBlack, ParkingSensors))
      router ! blackCarWithParkingSensors
      interestedParty expectMsg Car(color = "black", hasNavigation = false, hasParkingSensors = true)
    }
  }
}
