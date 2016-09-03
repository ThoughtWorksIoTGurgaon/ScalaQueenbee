package com.supersaiyyans.actors

import akka.actor.ActorSystem
import akka.testkit.{TestFSMRef, TestKit}
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

    it("Should update state data and stay in same state") {
      implicit val actorSystem = ActorSystem()

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      val data: ServiceData = ServiceData(name = "toggle bedroom light", serviceId = "SW101", deviceId = "DId101", state = SwitchServiceState("ON"))

      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef ! UpdateServiceData(data)
      serviceRepoActorRef.stateName should be(Running) // Todo - figure out if there is a way to say "should stay same"

      serviceRepoActorRef.stateData should be(ServicesData(Map("SW101" -> data)))
    }

    it("Should add new state data and stay in same state") {
      implicit val actorSystem = ActorSystem()

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      val switchServiceData = ServiceData(name = "toggle bedroom light", serviceId = "SW101", deviceId = "DId101", state = SwitchServiceState("ON"))

      serviceRepoActorRef ! AddService(switchServiceData)
      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef.stateData should be(ServicesData(Map("SW101" -> switchServiceData)))
    }

  }
}