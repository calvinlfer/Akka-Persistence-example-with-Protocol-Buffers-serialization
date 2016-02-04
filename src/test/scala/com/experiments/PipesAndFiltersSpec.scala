package com.experiments

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps


class PipesAndFiltersSpec
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
    "flow through when dealing with A Photo that contains a license plate and a high speed" in { // test
      val testMessageThatFlowsThrough = Photo("ABC-123", 120)

      licenseFilterRef ! testMessageThatFlowsThrough

      // assert that this message has come through on the test probe
      endProbe.expectMsg(testMessageThatFlowsThrough)
    }

    "not flow through when dealing with a Photo that contains a license plate and an adhering speed" in {
      val validLicenseGoodSpeed = Photo("ABC-123", goodSpeed)
      licenseFilterRef ! validLicenseGoodSpeed
      expectNoMsg(250 milliseconds)
    }

    "not flow through when dealing with a Photo that contains no license plate and high speed" in {
      val badLicenseHighSpeed = Photo("", highSpeed)
      licenseFilterRef ! badLicenseHighSpeed
      expectNoMsg(250 milliseconds)
    }

    "not flow through when dealing with a Photo that contains no license plate and good speed" in {
      val badLicenseGoodSpeed = Photo("", goodSpeed)
      licenseFilterRef ! badLicenseGoodSpeed
      expectNoMsg(250 milliseconds)
    }
  }

  """The pipe and filter enterprise integration pattern suite in the configuration where
    |input -> speed filter -> license filter -> output that filters messages accordingly """.stripMargin must {
    // suite + setup
    import PipesAndFilters._
    // Uses the "pipes-and-filters-test-system" implicitly
    // This is a test actor we can listen on and use in our test to do assertions
    val endProbe = TestProbe()

    // Define preset limits
    val speedLimit = 60
    val goodSpeed = 50
    val highSpeed = 120

    val licenseFilterRef = system.actorOf(Props(new LicenseFilter(endProbe.ref)))
    val speedFilterRef = system.actorOf(Props(new SpeedFilter(speedLimit, licenseFilterRef)))

    // We now have our configuration in place
    // speed filter -> license filter -> testProbe actor
    "flow through when dealing with A Photo that contains a license plate and a high speed" in {
      val testMessageThatFlowsThrough = Photo("ABC-123", 120)
      licenseFilterRef ! testMessageThatFlowsThrough
      endProbe.expectMsg(testMessageThatFlowsThrough)
    }

    "not flow through when dealing with a Photo that contains a license plate and an adhering speed" in {
      val validLicenseGoodSpeed = Photo("ABC-123", goodSpeed)
      licenseFilterRef ! validLicenseGoodSpeed
      expectNoMsg(250 milliseconds)
    }

    "not flow through when dealing with a Photo that contains no license plate and high speed" in {
      val badLicenseHighSpeed = Photo("", highSpeed)
      licenseFilterRef ! badLicenseHighSpeed
      expectNoMsg(250 milliseconds)
    }

    "not flow through when dealing with a Photo that contains no license plate and good speed" in {
      val badLicenseGoodSpeed = Photo("", goodSpeed)
      licenseFilterRef ! badLicenseGoodSpeed
      expectNoMsg(250 milliseconds)
    }
  }
}
