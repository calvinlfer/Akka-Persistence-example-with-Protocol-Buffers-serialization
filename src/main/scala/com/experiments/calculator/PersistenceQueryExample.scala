package com.experiments.calculator

import akka.Done
import akka.actor.ActorSystem
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.experiments.calculator.models._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * A simple example that uses Persistence Query to stream out the currents events from all the current persistent
  * actors (in our case, it's calculator-persistent-actor) from the LevelDB journal
  */
object PersistenceQueryExample extends App {
  implicit val system = ActorSystem("calculator-actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  // Configure Persistence Query
  val queries = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  val nonInvasiveLog = (x: String) => {
    println(x)
    x
  }

  // Grab a list of all the persistenceIds as of this moment
  val events = queries.currentPersistenceIds()
    .map(nonInvasiveLog)
    // currentEventsByPersistenceId will grab all the events as of this moment for a supplied persistence id
    .flatMapConcat(eachPersistentId => queries.currentEventsByPersistenceId(eachPersistentId))
    .map(eventEnvelope => eventEnvelope.event)
    .map({
      case Added(value) => s"Added $value"
      case Subtracted(value) => s"Subtracted $value"
      case Divided(value) => s"Divided $value"
      case Multiplied(value) => s"Multiplied $value"
      case Reset() => s"Value reset"
    })

  val matValue: Future[Done] = events.runWith(Sink.foreach(println))

  matValue.onComplete {
    case Success(done) =>
      Console println "Query completed successfully"
      system terminate()

    case Failure(e) =>
      Console println e
      system terminate()
  }
}
