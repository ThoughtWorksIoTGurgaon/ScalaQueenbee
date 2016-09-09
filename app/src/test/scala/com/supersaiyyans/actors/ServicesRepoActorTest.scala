package com.supersaiyyans.actors

import akka.actor.ActorSystem
import akka.testkit.TestFSMRef
import com.supersaiyyans.actors.ServicesRepoActor.Running
import org.scalatest.{Matchers, FunSpec}

class ServicesRepoActorTest extends FunSpec  with Matchers{


  describe("ServiceActorRepo Spec") {

    it("Should start actor with correct state") {
      implicit val actorSystem = ActorSystem()
      val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)
      serviceRepoActorRef.stateName.shouldBe(Running)

    }
  }

}
