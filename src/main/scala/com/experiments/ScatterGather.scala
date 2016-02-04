package com.experiments

import java.time.ZonedDateTime

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.FiniteDuration

/**
  * The Scatter Gather pattern which is made up of the recipient list pattern along with the aggregator pattern
  */
object ScatterGather {

  // This is the message that flows through our system
  case class PhotoMessage(id: String, photo: String, creationTime: Option[ZonedDateTime] = None,
                          speed: Option[Int] = None)

  // Capture the PhotoMessage in a TimeoutMessage case class to handle exceptional cases to prevent
  // the buffer size in the Aggregate from increasing if all the messages don't come in
  case class TimeoutMessage(msg: PhotoMessage)

  // For our example message, we mock the traffic cameras and image recognition tools by just providing the image.
  object ImageProcessing {
    def getSpeed(image: String): Option[Int] = {
      val attributes = image.split('|')
      if (attributes.length == 3) Some(attributes(1).toInt)
      else None
    }

    def getTime(image: String): Option[ZonedDateTime] = {
      val attributes = image.split('|')
      if (attributes.length == 3) Some(ZonedDateTime.parse(attributes(0)))
      else None
    }

    def getLicense(image: String): Option[String] = {
      val attributes = image.split('|')
      if (attributes.length == 3) Some(attributes(2))
      else None
    }

    def createPhotoString(date: ZonedDateTime, speed: Int, license: String): String =
      "%s|%s|%s".format(date.toString, speed, license)
  }

  // The workers in our scatter-gather pattern (these are part of the recipient list)
  class GetSpeedWorker(pipe: ActorRef) extends Actor {
    def receive = {
      case msg: PhotoMessage =>
        pipe ! msg.copy(speed = ImageProcessing.getSpeed(msg.photo))

    }
  }

  // The workers in our scatter-gather pattern (these are part of the recipient list)
  class GetTimeWorker(pipe: ActorRef) extends Actor {
    def receive = {
      case msg: PhotoMessage => pipe ! msg.copy(creationTime = ImageProcessing.getTime(msg.photo))
    }
  }

  // The scatterer takes the message and hands it to all in the recipient list
  class RecipientList(recipientList: Seq[ActorRef]) extends Actor {
    def receive = {
      case message: AnyRef => recipientList foreach { _ ! message }
    }
  }

  // note that the pipe is used in the context of enterprise integration patterns and it refers to the next
  // actor in the pipeline
  class Aggregator(timeoutBeforeMessageRemoval: FiniteDuration, pipe: ActorRef) extends Actor {
      val messages = ListBuffer[PhotoMessage]()
      // needed for the scheduler, use the actor system's dispatcher and not this actor's dispatcher
      implicit val executionContext = context.system.dispatcher

      // when an actor runs into exceptional circumstances, it will be restarted with the default supervision
      // strategy, take advantage of this hook to save all messages to the mailbox before restarting because
      // we lose internal state and the fact that the mailbox is still around
      override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
        // make sure we perform the normal lifecycle in addition to making our changes
        super.preRestart(reason, message)
        // send all messages to self (which ends up in the mailbox)
        messages foreach { self ! _ }

        // clean up this crashing actor's internal state which will be replaced with a new one
        // but given the same mailbox
        messages.clear()
      }

      def receive = {
        case rcvMessage: PhotoMessage =>
          messages.find(_.id == rcvMessage.id) match {
            case Some(alreadyReceivedMessage) =>
              val newCombinedMessage = PhotoMessage(
                rcvMessage.id,
                rcvMessage.photo,
                rcvMessage.creationTime.orElse(alreadyReceivedMessage.creationTime),
                rcvMessage.speed.orElse(alreadyReceivedMessage.speed)
              )
              // send the next actor the combined message
              pipe ! newCombinedMessage

              // remove the other part of the partial message from the list buffer
              messages -= alreadyReceivedMessage

            case None =>
              // This is the first part of the partial message
              messages += rcvMessage

              // Tell the scheduler to trigger a timeout with this message in case the other
              // part of the message does not come in time so we don't fill up the messages buffer
              context.system.scheduler.scheduleOnce(timeoutBeforeMessageRemoval, self, TimeoutMessage(rcvMessage))
          }

        // This case happens when the other part of the partial message doesn't come in time (sent by the scheduler)
        case TimeoutMessage(receivedMessage) =>
          // It's Option.map so the map will work provided there is something in the Option container otherwise
          // the map will not be executed
          messages find {_.id == receivedMessage.id} map {
            existingMessage =>
              // send the partial message we have
              pipe ! existingMessage
              // remove it to prevent needless increase of items
              messages -= existingMessage
          }

        // This is used for testing the scenario where the actor goes down, we give the test the opportunity to trigger
        // our Actor throw an exception and restart (default supervisor strategy)
        case ex: Exception => throw ex
      }
    }

  // The Gatherer is the Aggregator or the next actor receiving messages from the Aggregator
}

