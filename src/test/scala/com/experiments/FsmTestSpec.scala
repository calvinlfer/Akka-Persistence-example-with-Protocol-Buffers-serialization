package com.experiments

import akka.actor.FSM.{Transition, CurrentState, SubscribeTransitionCallBack}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.concurrent.duration._
import scala.language.postfixOps


class FsmTestSpec
  extends TestKit(ActorSystem("fsm-actor-test-system"))
    with WordSpecLike
    with BeforeAndAfterAll {

  // Make sure to shut down the actor system otherwise it will run forever
  override def afterAll() {
    system.terminate()
  }

  "The Inventory Actor FSM" must {
    // suite
    import com.experiments.models.Models._
    "transition through states to obtain books from the publisher and send a single book back to the requester" in {
      // test
      val NumberOfBooksPerRequest = 2
      val TotalNumberOfBooks = 2
      val publisherActor = system.actorOf(Props(new Publisher(NumberOfBooksPerRequest, TotalNumberOfBooks)))
      val inventoryActor = system.actorOf(Props(new Inventory(bookPublisher = publisherActor)))
      val stateProbe = TestProbe()
      val replyProbe = TestProbe()

      // Subscribe the stateProbe to the Inventory actor's FSM transition notifications
      inventoryActor ! SubscribeTransitionCallBack(stateProbe.ref)

      // The state probe should get back a message telling us the current Fsm state of the actor FSM it is observing
      stateProbe expectMsg CurrentState(fsmRef = inventoryActor, state = WaitForRequests)

      // Send an event to the inventory actor which will cause a state transition
      inventoryActor ! BookRequest(context = "Buy this book", target = replyProbe.ref)

      // Expect that the inventory Actor FSM made the transition WaitForRequests -> WaitForPublisher
      // since the Inventory actor FSM does not have any books when it starts
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForRequests, to = WaitForPublisher)

      // The publisher should send books over to the inventory actor FSM so the inventory actor FSM will make another
      // transition WaitForPublisher -> ProcessRequest
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForPublisher, to = ProcessRequest)

      // The inventory actor FSM should now be able to produce our request (note that the Entry action portion in the
      // FSM is responsible for doing this and sending a Done event to itself)
      // and will transition to WaitForRequest because there are no more requests
      // 1. The Inventory FSM should change to the WaitForRequest state because it has no more pending requests
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = ProcessRequest, to = WaitForRequests)

      // 2. The probe should get its book
      // Note that 1 in the Right container corresponds to the reserveId
      replyProbe expectMsg BookReply("Buy this book", Right[String, Int](1))

      // Ask for another book
      inventoryActor ! BookRequest(context = "Buy another book", target = replyProbe.ref)
      // Since the inventory actor already has some books, it will use that and won't need to wait for the publisher
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForRequests, to = ProcessRequest)
      replyProbe expectMsg BookReply("Buy another book", Right[String, Int](2))
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = ProcessRequest, to = WaitForRequests)

      // Ask for one more book (this time the inventory is exhausted) and the publisher has also run out of books
      // Pretend the requester has asked for a book that is out-of-print
      inventoryActor ! BookRequest(context = "Last one I promise :-)", target = replyProbe.ref)
      // The inventory actor has no more books, so it asks the publisher
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForRequests, to = WaitForPublisher)

      // The publisher says it has no more books so the inventory actor FSM transitions to the ProcessSoldOut state
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForPublisher, to = ProcessSoldOut)

      // The requester gets a sold out message
      replyProbe expectMsg BookReply("Last one I promise :-)", Left[String, Int]("Sold Out"))
    }

    "process multiple book requests" in {
      // test
      val NumberOfBooksPerRequest = 2
      val TotalNumberOfBooks = 2
      val publisherActor = system.actorOf(Props(new Publisher(NumberOfBooksPerRequest, TotalNumberOfBooks)))
      val inventoryActor = system.actorOf(Props(new Inventory(bookPublisher = publisherActor)))
      val stateProbe = TestProbe()
      val replyProbe = TestProbe()

      // Subscribe the stateProbe to the Inventory actor's FSM transition notifications
      inventoryActor ! SubscribeTransitionCallBack(stateProbe.ref)

      // The state probe should get back a message telling us the current Fsm state of the actor FSM it is observing
      stateProbe expectMsg CurrentState(fsmRef = inventoryActor, state = WaitForRequests)

      // Send 2 book requests
      inventoryActor ! BookRequest(context = "Buy this book", target = replyProbe.ref)
      inventoryActor ! BookRequest(context = "Buy another book", target = replyProbe.ref)

      // the Inventory actor FSM does not have any books when it starts so it must ask the publisher
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForRequests, to = WaitForPublisher)
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForPublisher, to = ProcessRequest)

      // first request has been processed so it goes back to WaitForRequests
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = ProcessRequest, to = WaitForRequests)

      // It sees it has pending requests so it goes to processing again (this time it has books in the inventory)
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForRequests, to = ProcessRequest)
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = ProcessRequest, to = WaitForRequests)

      // Expect 2 book responses
      replyProbe expectMsg BookReply("Buy this book", Right[String, Int](1))
      replyProbe expectMsg BookReply("Buy another book", Right[String, Int](2))
    }

    "trigger a timeout message for an unresponsive publisher which will cause a resend of message to publisher" in {
      // test
      // We will control the publisher
      val publisherProbe = TestProbe()
      val inventoryActor = system.actorOf(Props(new Inventory(bookPublisher = publisherProbe.ref)))
      val stateProbe = TestProbe()
      val replyProbe = TestProbe()

      // Subscribe the stateProbe to the Inventory actor's FSM transition notifications
      inventoryActor ! SubscribeTransitionCallBack(stateProbe.ref)

      // The state probe should get back a message telling us the current Fsm state of the actor FSM it is observing
      stateProbe expectMsg CurrentState(fsmRef = inventoryActor, state = WaitForRequests)

      // Send a book request
      inventoryActor ! BookRequest(context = "Buy this book", target = replyProbe.ref)

      // the Inventory actor FSM does not have any books when it starts so it must ask the publisher
      // (which is unresponsive)
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForRequests, to = WaitForPublisher)

      // Make sure the publisher receives the request from the inventory actor
      publisherProbe expectMsg PublisherRequest

      // Timeout is triggered after 5 seconds
      // It goes back to the WaitForRequests and it has pending requests
      stateProbe.expectMsg(5.1 seconds, Transition(fsmRef = inventoryActor, from = WaitForPublisher, to = WaitForRequests))

      // The pending requests will make it go back to WaitForPublisher (which resends the message)
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForRequests, to = WaitForPublisher)

      // Now let's make the publisher respond this time
      publisherProbe expectMsg PublisherRequest
      publisherProbe.send(actor = inventoryActor, msg = BookSupply(10))

      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = WaitForPublisher, to = ProcessRequest)

      //request has been processed so it goes back to WaitForRequests
      stateProbe expectMsg Transition(fsmRef = inventoryActor, from = ProcessRequest, to = WaitForRequests)

      replyProbe expectMsg BookReply("Buy this book", Right[String, Int](1))
    }
  }
}
