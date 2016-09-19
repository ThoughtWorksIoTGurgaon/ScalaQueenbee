package com.supersaiyyans.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import com.supersaiyyans.actors.ServicesRepoActor._
import org.scalatest.{FunSpecLike, Matchers}
import com.supersaiyyans.actors.CommonMessages.{ServiceData, SwitchServiceState}

class ServicesRepoActorTest extends TestKit(ActorSystem("name")) with Matchers with FunSpecLike with ImplicitSender {


  describe("ServiceActorRepo Spec") {

    it("Should start actor with correct state") {

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef.stateData should be(ServicesData(Map()))
    }

    it("Should update state data and stay in same state") {
      

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      val data: ServiceData = ServiceData(name = "toggle bedroom light", "SW101DId101".hashCode, state = SwitchServiceState("ON"))

      serviceRepoActorRef.stateName should be(Running)

      val uuid = "somedevice-service".hashCode
      serviceRepoActorRef ! UpdateServiceData(uuid, data)
      serviceRepoActorRef.stateName should be(Running) // Todo - figure out if there is a way to say "should stay same"

      serviceRepoActorRef.stateData should be(ServicesData(Map(uuid -> data)))
    }

    it("Should add new state data and stay in same state") {

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      val switchServiceData = ServiceData(name = "toggle bedroom light", "SW101DId101".hashCode, state = SwitchServiceState("ON"))

      val uuid = "somedevice-service".hashCode

      serviceRepoActorRef ! AddService(uuid, switchServiceData)
      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef.stateData should be(ServicesData(Map(uuid -> switchServiceData)))
    }

    it("Should notify sender about unexpected message if the message is not received/present") {

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      serviceRepoActorRef ! "Some Unknown Message"

      expectMsg(UnexpectedMessage)
      serviceRepoActorRef.stateName should be(Running)
    }


  }
}