package scala.com.supersaiyyans.actors

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{TestFSMRef, TestKit, TestProbe}
import com.supersaiyyans.actors.ServiceActors.SupportedChannelTypes.MQTT
import com.supersaiyyans.actors.{ServiceActor, SwitchServiceActor}
import org.scalatest.{FunSpecLike, Matchers}

class SwitchServiceActorTest extends TestKit(ActorSystem("SwitchServiceActorSystem")) with Matchers with FunSpecLike {


  val testProbe = TestProbe()
  trait MockChannelDecider {
    _: ServiceActor =>


    override val channelActor = testProbe.testActor

  }


  val serviceRepoActor = TestProbe()

//  describe("") {
//    implicit val actorSystem = ActorSystem()
//
//    class SwitchServiceTestActor()
//      extends SwitchServiceActor("someDevice", "someServiceId", SwitchServiceData("Off"), serviceRepoActor.testActor, MQTT) with MockChannelDecider
//
//    it("Should change the switch to on when it gets the message on") {
//
////      val switchServiceTestActorRef = TestFSMRef(new SwitchServiceTestActor())
//
////      switchServiceTestActorRef ! "ON"
//
////      expectMsg()
//
//
//
//    }
//  }


}
