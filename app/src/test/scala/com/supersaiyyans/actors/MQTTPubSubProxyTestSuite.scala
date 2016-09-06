package com.supersaiyyans.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit._
import net.sigusr.mqtt.api.{Connect, Connected, Message}
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, Matchers}

class MQTTPubSubProxyTestSuite extends TestKit(ActorSystem("MySpec"))
  with FunSpecLike with Matchers with BeforeAndAfterAll {


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  describe("A MQTTPubSubProxy Actor with its parent and child") {
    val mockMQTTManager = TestActorRef(Props(new Actor {
      def receive = {
        case x:Connect =>
          sender ! Connected
        case msg@_=>
          sender ! msg
      }
    }))

    val mockListener = TestProbe()

    class TestMQTTSupervisor(override val deviceProxy: ActorRef = mockMQTTManager)
      extends MQTTPubSubProxySupervisor(mockListener.ref,"someDeviceId")



    it("Should move to ready state once connected") {

      val testMQTTPubSubProxy = TestFSMRef(new TestMQTTSupervisor())
      testMQTTPubSubProxy.stateName.shouldBe(DeviceProxySupervisor.Disconnected)

      mockMQTTManager.tell(Connected,testMQTTPubSubProxy)

      testMQTTPubSubProxy.stateName.shouldBe(DeviceProxySupervisor.Connected)

      mockMQTTManager.tell(Message("someTopic",Vector(1.toByte)),testMQTTPubSubProxy)

      mockListener.expectMsg(Message)



    }
  }
}
