package com.supersaiyyans.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestFSMRef, TestKit}
import com.supersaiyyans.actors.ServicesRepoActor._
import org.scalatest.{FunSpecLike, Matchers}

class ServicesRepoActorTest extends TestKit(ActorSystem("name")) with Matchers with FunSpecLike with ImplicitSender {


  describe("ServiceActorRepo Spec") {

    it("Should start actor with correct state") {

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef.stateData should be(ServicesData(Map()))
    }

    it("Should update state data and stay in same state") {
      

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      val data: ServiceData = ServiceData(name = "toggle bedroom light", serviceId = "SW101", deviceId = "DId101", state = SwitchServiceState("ON"))

      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef ! UpdateServiceData(data)
      serviceRepoActorRef.stateName should be(Running) // Todo - figure out if there is a way to say "should stay same"

      serviceRepoActorRef.stateData should be(ServicesData(Map("SW101" -> data)))
    }

    it("Should add new state data and stay in same state") {

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      val switchServiceData = ServiceData(name = "toggle bedroom light", serviceId = "SW101", deviceId = "DId101", state = SwitchServiceState("ON"))

      serviceRepoActorRef ! AddService(switchServiceData)
      serviceRepoActorRef.stateName should be(Running)
      serviceRepoActorRef.stateData should be(ServicesData(Map("SW101" -> switchServiceData)))
    }

    it("Should notify sender about unexpected message if the message is not received/present") {

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      serviceRepoActorRef ! "Some Unknown Message"

      expectMsg(UnexpectedMessage)
      serviceRepoActorRef.stateName should be(Running)
    }


  }
}