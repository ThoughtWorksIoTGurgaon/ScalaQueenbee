package com.supersaiyyans.actors

import java.util.UUID

import akka.testkit.{TestFSMRef, TestKit}
import com.supersaiyyans.actors.TheEnchantress.{AddServiceActor, DiscoveredService, EnchantressData}
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{FunSpecLike, Matchers}
import com.supersaiyyans.util.Commons.AssignedServiceId


class EnchantressTest extends TestKit(ActorSystem("TheEnchantressTest")) with Matchers with FunSpecLike {


  describe("An enchantress test setup") {
    val repoActorProbe = TestProbe()
    val enchantressTestActor = TestFSMRef(new TheEnchantress(repoActorProbe.testActor, List.empty))

    it("Should be able to add new services") {
      val newServiceActor = TestProbe().testActor

      val uuid: AssignedServiceId = "my-device-service".hashCode
      enchantressTestActor ! AddServiceActor(uuid, newServiceActor)
      enchantressTestActor.stateData.shouldBe(EnchantressData(Map(uuid->newServiceActor)))
    }

    it("Should create a new Switch Service Actor for a newly discovered service") {
      enchantressTestActor ! DiscoveredService("someDeviceId", "someServiceId","1")
      enchantressTestActor.stateData match {
        case EnchantressData(x) =>
          x.size shouldBe(1)
      }
    }

  }


}

object EnchantressTest {


}
