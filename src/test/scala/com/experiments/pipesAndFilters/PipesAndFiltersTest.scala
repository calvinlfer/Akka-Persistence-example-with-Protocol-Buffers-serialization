package com.experiments.pipesAndFilters

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestProbe, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.concurrent.duration._
import scala.language.postfixOps


class PipesAndFiltersTest
  extends TestKit(ActorSystem("pipes-and-filters-test-system"))
    with WordSpecLike
    with BeforeAndAfterAll {

  // Make sure to shut down the actor system otherwise it will run forever
  override def afterAll() {
    system.terminate()
  }

  """The pipe and filter enterprise integration pattern suite in the configuration where
    |input -> license filter -> speed filter -> output that filters messages accordingly """.stripMargin must {
    // suite + setup
    import PipesAndFilters._
    // Uses the "pipes-and-filters-test-system" implicitly
    // This is a test actor we can listen on and use in our test to do assertions
    val endProbe = TestProbe()

    // Define preset limits
    val speedLimit = 60
    val goodSpeed = 50
    val highSpeed = 120

    // Create the SpeedFilter actor and hook it up to the endProbe test actor
    val speedFilterRef = system.actorOf(Props(new SpeedFilter(speedLimit, endProbe.ref)))

    // Create a LicenseFilter actor and hook it up to speedFilterRef to complete the configuration
    val licenseFilterRef = system.actorOf(Props(new LicenseFilter(speedFilterRef)))

    // We now have our configuration in place
    // license filter -> speed filter -> testProbe actor
    "A Photo that contains a license plate and a high speed must flow through" in { // test
      val testMessageThatFlowsThrough = Photo("ABC-123", 120)

      licenseFilterRef ! testMessageThatFlowsThrough

      // assert that this message has come through on the test probe
      endProbe.expectMsg(testMessageThatFlowsThrough)
    }

    "A Photo that contains a license plate and an adhering speed must not flow through" in {
      val validLicenseGoodSpeed = Photo("ABC-123", goodSpeed)
      licenseFilterRef ! validLicenseGoodSpeed
      expectNoMsg(250 milliseconds)
    }

    "A Photo that contains no license plate and high speed must not flow through" in {
      val badLicenseHighSpeed = Photo("", highSpeed)
      licenseFilterRef ! badLicenseHighSpeed
      expectNoMsg(250 milliseconds)
    }

    "A Photo that contains no license plate and good speed must not flow through" in {
      val badLicenseGoodSpeed = Photo("", goodSpeed)
      licenseFilterRef ! badLicenseGoodSpeed
      expectNoMsg(250 milliseconds)
    }
  }
}
