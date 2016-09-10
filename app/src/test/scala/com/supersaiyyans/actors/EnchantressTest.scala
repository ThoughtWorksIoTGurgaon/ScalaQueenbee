package com.supersaiyyans.actors

import akka.testkit.{TestFSMRef, TestKit}
import com.supersaiyyans.actors.TheEnchantress.{AddServiceActor, EnchantressData, ServiceDiscovered}
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{FunSpecLike, Matchers}


class EnchantressTest extends TestKit(ActorSystem("TheEnchantressTest")) with Matchers with FunSpecLike {


  describe("An enchantress test setup") {
    val repoActorProbe = TestProbe()
    val enchantressTestActor = TestFSMRef(new TheEnchantress(repoActorProbe.testActor, List.empty))

    it("Should be able to add new services") {
      val newServiceActor = TestProbe().testActor

      enchantressTestActor ! AddServiceActor(newServiceActor)
      enchantressTestActor.stateData.shouldBe(EnchantressData(Seq(newServiceActor)))
    }

    it("Should create a new Switch Service Actor for a newly discovered service") {
      enchantressTestActor ! ServiceDiscovered("someDeviceId", "someServiceId","1")
      enchantressTestActor.stateData match {
        case EnchantressData(x) =>
          x.size shouldBe(1)
      }
    }

  }


}

object EnchantressTest {


}
