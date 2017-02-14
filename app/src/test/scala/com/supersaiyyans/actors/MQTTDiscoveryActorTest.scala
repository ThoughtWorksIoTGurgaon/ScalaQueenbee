package src.test.scala.com.supersaiyyans.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit, TestProbe}
import com.supersaiyyans.actors.MQTTDiscoveryActor
import com.typesafe.config.ConfigFactory
import net.sigusr.mqtt.api.Connect
import org.scalatest.{FunSpecLike, FunSuite, FunSuiteLike, ShouldMatchers}

import scala.concurrent.duration._
import scala.language.postfixOps


class MQTTDiscoveryActorTest() extends TestKit(ActorSystem("MQTTDiscoveryActor")) with FunSpecLike with ShouldMatchers with ImplicitSender {

  val dummyMqttManager = TestProbe()

  describe("MQTT Discovery actor") {
    it("Should try to send connection messages to MQTT manager immediately and then every configured time") {
      val mqttDiscoveryActor = system.actorOf(Props(new MQTTDiscoveryActor(ConfigFactory.load().getConfig("mqtt")) {
        override val mqttManager = dummyMqttManager.ref
      }))

      dummyMqttManager.expectMsg(1 seconds, Connect("QUEENBEE_MQTT_DISCOVERY_ACTOR_CONNECTING"))
    }
  }



}
