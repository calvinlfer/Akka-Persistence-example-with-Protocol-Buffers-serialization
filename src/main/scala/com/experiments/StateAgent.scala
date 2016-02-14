package com.experiments

import akka.actor.ActorSystem
import akka.agent.Agent
import scala.concurrent.Future
import scala.language.postfixOps


object StateAgent {

  // Agent models
  object AgentModels {
    type BookName = String

    case class BookStatistics(val nameBook: BookName, nrSold: Int)

    // The state object
    case class StateBookStatistics(val sequence: Long, books: Map[BookName, BookStatistics])

  }

  /**
    * Class that encapsulates the agent along with the setter and getter API which talks to the agent
    * An Akka agent is used to encapsulate the access to shared state between actors in a non locking fashion
    *
    * @param system we use the actor system's dispatcher for the execution context
    */
  class BookStatisticsManager(system: ActorSystem) {

    import AgentModels._

    implicit val executionContext = system.dispatcher
    private val stateAgent = Agent(StateBookStatistics(sequence = 0, Map[BookName, BookStatistics]()))

    // Updates the state of the agent (setter)
    def addBooksSold(name: BookName, nrOfBooksSold: Int): Unit = {
      stateAgent send {
        oldStateBookStats =>
          // check whether the book being added, already exists in the map
          val updatedBookStat = oldStateBookStats.books.get(name) match {
            // already exists so update the entry
            case Some(bookStatistics) =>
              bookStatistics.copy(nrSold = bookStatistics.nrSold + nrOfBooksSold)
            // does not exist
            case None =>
              BookStatistics(name, nrOfBooksSold)
          }
          // Return new state
          oldStateBookStats.copy(oldStateBookStats.sequence + 1, oldStateBookStats.books + (name -> updatedBookStat))
      }
    }

    // Same as above but in functional style (setter)
    def addBooksSoldAndObtainNewState(name: BookName, nrOfBooksSold: Int): Future[StateBookStatistics] =
      stateAgent alter {
        oldStateBookStats =>
          // check whether the book being added, already exists in the map
          val updatedBookStat = oldStateBookStats.books.get(name) match {
            // already exists so update the entry
            case Some(bookStatistics) =>
              bookStatistics.copy(nrSold = bookStatistics.nrSold + nrOfBooksSold)
            // does not exist
            case None =>
              BookStatistics(name, nrOfBooksSold)
          }
          // Return new state
          oldStateBookStats.copy(oldStateBookStats.sequence + 1, oldStateBookStats.books + (name -> updatedBookStat))
      }

    // getter
    def stateBookStatistics: StateBookStatistics =
      stateAgent.get()
  }

}