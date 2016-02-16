package com.experiments

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  * Created by cfernandes on 2016-02-16.
  */
class TestSpec extends TestKit(ActorSystem("actor-test-system"))
  with WordSpecLike
  with BeforeAndAfterAll {

}
