package com.supersaiyyans.actors

import akka.actor.ActorSystem
import akka.testkit.TestFSMRef
import com.supersaiyyans.actors.ServicesRepoActor._
import org.scalatest.{FunSpec, Matchers}

class ServicesRepoActorTest extends FunSpec with Matchers {


  describe("ServiceActorRepo Spec") {

    it("Should start actor with correct state") {
      implicit val actorSystem = ActorSystem()

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef.stateData should be(ServicesData(Map()))

    }
  }

  describe("ServiceActorRepo Spec") {

    it("Should update state data and stay in same state") {
      implicit val actorSystem = ActorSystem()

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      val data: ServiceData = ServiceData(name = "toggle bedroom light", serviceId = "SW101", deviceId = "DId101", state = SwitchServiceState("ON"))
      serviceRepoActorRef ! UpdateState(data)

      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef.stateData should be(ServicesData(Map("SW101" -> data)))
    }
  }

}
