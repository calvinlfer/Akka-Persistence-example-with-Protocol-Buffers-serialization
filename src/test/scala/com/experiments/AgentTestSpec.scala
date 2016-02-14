package com.experiments

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpecLike }

import scala.language.postfixOps

class AgentTestSpec extends TestKit(ActorSystem("agent-actor-test-system"))
  with WordSpecLike
  with MustMatchers
  with ScalaFutures
  with BeforeAndAfterAll {
  "The Agents suite" must {
    import com.experiments.StateAgent._
    import AgentModels._

    "be able to update the state of an agent and retrieve it in a synchronous fashion" in {
      val agent = new BookStatisticsManager(system)
      agent.addBooksSold(name = "Akka in Action", nrOfBooksSold = 1)
      // wait for change to propagate
      Thread.sleep(500)
      agent.stateBookStatistics must be {
        StateBookStatistics(sequence = 1, Map("Akka in Action" -> BookStatistics("Akka in Action", 1)))
      }
    }

    "be able to update the state of an agent and retrieve it in a non-blocking fashion" in {
      val agent = new BookStatisticsManager(system)
      implicit val executionContext = system.dispatcher
      whenReady(agent.addBooksSoldAndObtainNewState(name = "Akka in Action", nrOfBooksSold = 1)) {
        _ must be {
          StateBookStatistics(sequence = 1, Map("Akka in Action" -> BookStatistics("Akka in Action", 1)))
        }
      }
    }
  }
}
