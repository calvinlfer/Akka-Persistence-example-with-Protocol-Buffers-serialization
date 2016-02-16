package com.experiments

import java.io.File

import akka.actor.ActorSystem
import org.apache.commons.io.FileUtils

import scala.util.Try

/**
  * The PersistenceCleanup trait defines a deleteStorageLocations method that removes directories created by the
  * leveldb journal (as well as the default snapshot journal). It gets the configured directories from the Akka
  * configuration.
  */
trait PersistenceCleanup {
  def system: ActorSystem

  // Obtain information about the event journal and snapshot journal from the Akka configuration
  val storageLocations =
    List(
      "akka.persistence.journal.leveldb.dir",
      "akka.persistence.journal.leveldb-shared.store.dir",
      "akka.persistence.snapshot-store.local.dir").map { s =>
        new File(system.settings.config.getString(s))
      }

  def deleteStorageLocations(): Unit = {
    storageLocations.foreach(dir => Try(FileUtils.deleteDirectory(dir)))
  }
}