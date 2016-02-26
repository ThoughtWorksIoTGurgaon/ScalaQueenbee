package src.main.scala.com.supersaiyyans.actors

import src.main.scala.com.supersaiyyans.service.DiscoveryService
import java.net.{InetSocketAddress, InetAddress}

import akka.actor.Actor
import net.sigusr.mqtt.api._

class MQTTSubscriberProxy extends Actor {

  val MQTTPORT = 1883
  val MQTTHOST = "192.168.43.11"
  val subscribeTopic = "/device/+/data"

  val mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(InetAddress.getByName(MQTTHOST), MQTTPORT)))

  mqttManager ! Connect("SCALA_DISCOVERY_ACTOR")

  def receive = {
    case Connected =>
      println("-------------------Discovery Actor Connected--------------------")
      mqttManager ! Subscribe(Vector((subscribeTopic, AtMostOnce)), MessageId(10))
      context become ready
  }

  def ready: Receive = {
    case Message(topic, byteVector) =>
      val TopicDeviceExtractor = "(\\/device\\/)([a-z|-]*)(\\/data)".r
      println(s"Topic - ${topic}")
      topic match {
        case TopicDeviceExtractor(_, deviceId: String, _) =>
          println("Received a message from device: " + deviceId)
          DiscoveryService.process(deviceId, byteVector)
        case _ => println("Unknown message received")
      }
      println("Received: " + byteVector)
    case x@_ =>
      println(s"Unknown message received:${x}")
  }

}
