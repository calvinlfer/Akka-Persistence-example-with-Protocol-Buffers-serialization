package com.experiments

import akka.actor.{ActorSystem, ActorRef}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.Config
import org.scalatest.{WordSpecLike, Matchers, BeforeAndAfterAll}

/**
  * The PersistenceSpec deletes any leftover directories before the unit test starts, and deletes the directories
  * after all specifications and shuts down the actor system used during the test.
  * @param system the test actor system
  */
abstract class PersistenceSpec(system: ActorSystem) extends TestKit(system)
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with PersistenceCleanup {

  def this(name: String, config: Config) = this(ActorSystem(name, config))
  override protected def beforeAll() = deleteStorageLocations()

  override protected def afterAll() = {
    deleteStorageLocations()
    TestKit.shutdownActorSystem(system)
  }

  def killActors(actors: ActorRef*) = {
    actors.foreach { actor =>
      watch(actor)
      system.stop(actor)
      expectTerminated(actor)
    }
  }
}
