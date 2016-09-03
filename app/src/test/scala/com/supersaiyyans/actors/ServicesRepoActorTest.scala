package com.supersaiyyans.actors

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestFSMRef, TestKit}
import com.supersaiyyans.actors.ServicesRepoActor._
import org.scalatest.{FunSpec, Matchers}

case object UnexpectedMessage  // Todo : Make it more specific
case object SomeUnknownMessage

class DummyActor(actorRef : ActorRef) extends Actor {

  def sendUnexpectedMessage = actorRef ! SomeUnknownMessage

  var recievedUnxpectedMessage = false

  override def receive: Receive = {
    case UnexpectedMessage => recievedUnxpectedMessage = true
  }
}


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

    it("Should notify sender about unexpected message if the message is not received/present") {
      implicit val actorSystem = ActorSystem()

      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

      val ref: TestActorRef[DummyActor] = TestActorRef(Props(new DummyActor(serviceRepoActorRef)))
      val actor: DummyActor = ref.underlyingActor

      actor.sendUnexpectedMessage

      serviceRepoActorRef.stateName should be(Running)
      actor.recievedUnxpectedMessage should be(true)
    }


  }
}