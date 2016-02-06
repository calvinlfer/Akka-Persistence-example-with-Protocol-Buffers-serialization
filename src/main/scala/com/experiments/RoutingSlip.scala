package com.experiments

import akka.actor.{ActorLogging, Props, Actor, ActorRef}

/**
  * This is also known as the dynamic pipes-and-filters pattern
  * Depending on the message, it can flow through different pipelines
  */
object RoutingSlip {

  object CarOption extends Enumeration {
    // so we don't need to say CarOption.Value to refer to any members
    // belonging to this enum space, we can say that ColorGray is a
    // CarOption
    type CarOption = Value
    val ColorBlack, ColorGray, Navigation, ParkingSensors = Value
  }

  import CarOption._
  // The incoming message that comes into the Slip Router
  case class Order(options: Seq[CarOption])
  case class Car(color: String = "", hasNavigation: Boolean = false, hasParkingSensors: Boolean = false)

  // This is the message sent between the tasks
  case class RouteSlipMessage(routeSlip: Seq[ActorRef], message: AnyRef)

  // All actors need this trait for the routing slip pattern so they know who to send the message to next
  trait RouteSlip {
    def sendMessageToNextTask(routeSlip: Seq[ActorRef], message: AnyRef): Unit = {
      // get the next step
      val nextTask = routeSlip.head
      val newSlip = routeSlip.tail
      // end of the pipeline - everyone has signed off and nobody left
      if (newSlip.isEmpty) {
        nextTask ! message
      } else {
        // Keep going and passing on the message through the RouteSlipMessage
        // Send it to the next recipient but only send the tail of the routeSlip
        nextTask ! RouteSlipMessage(newSlip, message)
      }
    }
  }

  // Each Actor that does a bit of work and passes the message on
  class PaintCar(color: String) extends Actor with RouteSlip with ActorLogging {
    def receive = {
      case RouteSlipMessage(routeSlip: Seq[ActorRef], car: Car) =>
        log.info(s"PaintCar($color)")
        log.info(s"Car: $car")
        // paint the car
        val paintedCar = car.copy(color=color)
        // pass the message on
        sendMessageToNextTask(routeSlip, paintedCar)
    }
  }

  // Each Actor that does a bit of work and passes the message on
  class AddNavigation extends Actor with RouteSlip with ActorLogging {
    def receive = {
      case RouteSlipMessage(routeSlip: Seq[ActorRef], car: Car) =>
        log.info(s"Car: $car")
        val carWithNavigation = car.copy(hasNavigation = true)
        sendMessageToNextTask(routeSlip, carWithNavigation)
    }
  }

  // Each Actor that does a bit of work and passes the message on
  class AddParkingSensors extends Actor with RouteSlip with ActorLogging {
    def receive = {
      case RouteSlipMessage(routeSlip: Seq[ActorRef], car: Car) =>
        log.info(s"Car: $car")
        val carWithParkingSensors = car.copy(hasParkingSensors = true)
        sendMessageToNextTask(routeSlip, carWithParkingSensors)
    }
  }

  /**
    * This guy is the decider and based on the incoming message, he will decide
    * who does the work and gets to work on the car and sets up the pipeline of
    * actors to do the work dynamically based on the input
    *
    * Note that endStep is the party interested in the finished product
    */
  class SlipRouter(endStep: ActorRef) extends Actor with RouteSlip with ActorLogging {
    // The SlipRouter manages the pipeline of actors responsible for doing work
    val paintBlackActor = context.actorOf(Props(new PaintCar("black")), "paintBlack")
    val paintGrayActor = context.actorOf(Props(new PaintCar("gray")), "paintGray")
    val navigationActor = context.actorOf(Props[AddNavigation], "navigation")
    val parkingSensorsActor = context.actorOf(Props[AddParkingSensors], "parkingSensor")

    /**
      * Based on the message, we will construct the pipeline dynamically!
      * Also we add the party who is interested to the end of the list so they get the finished product
      * Note: map also filters out invalid patterns because map uses a partial function
      *
      * @param order the incoming order
      * @return a sequence of actors that represent the dynamically created pipeline
      */
    private def createRouteSlipFromOrder(order: Order): Seq[ActorRef] =
      (order.options map {
        case ColorBlack => paintBlackActor
        case ColorGray => paintGrayActor
        case Navigation => navigationActor
        case ParkingSensors => parkingSensorsActor
      }) :+ endStep

    def receive = {
      case order: Order =>
        log.info(s"Incoming order: $order")
        val routeSlip = createRouteSlipFromOrder(order)
        sendMessageToNextTask(routeSlip, Car())
    }
  }
}
