package com.experiments.pipesAndFilters

import akka.actor.{Actor, ActorRef}


object PipesAndFilters {

  case class Photo(license: String, speed: Int)

  /**
    * This is responsible for checking if the license is present in the message
    * A Filter in the pipes-and-filters pattern
    * Here pipe is in the context of the filters and pipes pattern
    * @param pipe the next actor
    */
  class LicenseFilter(pipe: ActorRef) extends Actor {
    def receive = {
      // We use @ to capture the entire Photo in the message value
      // since we do extraction
      case message @ Photo(license, speed) =>
        if (!license.isEmpty) pipe ! message
    }
  }

  /**
    * This is responsible for checking if the speed has been exceeded
    * A Filter in the pipes-and-filters pattern
    * Here pipe is in the context of the filters and pipes pattern
    * @param minSpeed the minimum speed
    * @param pipe the next actor
    */
  class SpeedFilter(minSpeed: Int, pipe: ActorRef) extends Actor {
    def receive = {
      case message @ Photo(license, speed) =>
        if (speed > minSpeed) pipe ! message
    }
  }
}
