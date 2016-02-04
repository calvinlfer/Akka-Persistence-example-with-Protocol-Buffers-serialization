package com.experiments

import java.time.ZonedDateTime

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.NoStackTrace

class ScatterGathererSpec
  extends TestKit(ActorSystem("scatter-gatherer-test-system"))
    with WordSpecLike
    with BeforeAndAfterAll {

  import ScatterGather._

  // Make sure to shut down the actor system otherwise it will run forever
  override def afterAll() {
    system.terminate()
  }

  "The Recipient-List component of the scatter gatherer enterprise integration pattern" must {  // suite + setup
    case class TestMessage(message: String)
    val helloMessage = TestMessage("Hello")

    // Create test probe actors that will be the recipients in the recipient list
    val endProbeα = TestProbe()
    val endProbeβ = TestProbe()
    val endProbeγ = TestProbe()

    val scatterActor = system.actorOf(Props(new RecipientList(Seq(endProbeα.ref, endProbeβ.ref, endProbeγ.ref))))

    "scatter the message" in {  // test
      // Send a message to the scatter actor and make sure all endProbes see the message
      scatterActor ! helloMessage

      endProbeα expectMsg helloMessage
      endProbeβ expectMsg helloMessage
      endProbeγ expectMsg helloMessage
    }
  }

  "The Aggregator component of the scatter gatherer enterprise integration pattern" must { // suite + setup
    val endProbe = TestProbe()
    val timeNow = ZonedDateTime.now()
    val aggregatorActor = system.actorOf(Props(new Aggregator(1 second, endProbe.ref)))
    val photoStr = ImageProcessing.createPhotoString(timeNow, 60, "ABC-123")

    // Set up two partial filled out messages with the same ID that emulate the workers
    val messageα = PhotoMessage("id1", photoStr, Some(timeNow), None)
    val messageβ = PhotoMessage("id1", photoStr, None, Some(60))
    val messageCombined = PhotoMessage("id1", photoStr, Some(timeNow), Some(60))

    "aggregate partial messages with the same identity into a combined message" in {
      // Send both messages
      aggregatorActor ! messageα
      aggregatorActor ! messageβ

      // Assert the combination message is received on the other end
      endProbe.expectMsg(messageCombined)
    }

    "send out the 1st part of the partial message if the other message is not sent in time" in {
      // Send one message
      aggregatorActor ! messageα
      // Give the scheduler some time to generate a Timeout message which will tell the aggregator
      // to just send what it has
      endProbe.expectMsg(messageα)
    }

    "be able to combine partial messages even if the actor restarted" in {
      // Send the one part of the message
      aggregatorActor ! messageα

      // Restart it by making it throw an exception (remove the exception stack trace using the mixed in trait)
      aggregatorActor ! new IllegalStateException("Oh no") with NoStackTrace

      // Send the other part of the message
      aggregatorActor ! messageβ

      // Expect the combined message on the other side
      endProbe.expectMsg(messageCombined)
    }
  }

  "The Scatter Gatherer enterprise integration pattern" must { // suite + setup
    // Monitor the end of the pipeline with a actor probe
    val endProbe = TestProbe()

    // Set up the gatherer
    val aggregatorActor = system.actorOf(Props(new Aggregator(1 second, endProbe.ref)))

    // Set up the workers to push their work to the aggregatorActor
    val speedActor = system.actorOf(Props(new GetSpeedWorker(aggregatorActor)))
    val timeActor = system.actorOf(Props(new GetTimeWorker(aggregatorActor)))

    // Create the Scatterer
    val scattererActor = system.actorOf(Props(new RecipientList(Seq(speedActor, timeActor))))

    // Sample data that will be parsed by the system
    val timeNow = ZonedDateTime.now()
    val photoStr = ImageProcessing.createPhotoString(timeNow, 60, "ABC-123")
    val sampleData = PhotoMessage("id1", photoStr)
    val pipelineOutput = PhotoMessage("id1", photoStr, Some(timeNow), Some(60))

    """scatter the message to its workers for cooperative multitasking and gather the individual work together to
      |produce a combined result""".stripMargin in {
      scattererActor ! sampleData
      endProbe.expectMsg(pipelineOutput)
    }
  }
}