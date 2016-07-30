package com.supersaiyyans.actors

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.Actor
import com.supersaiyyans.service.DiscoveryService
import com.typesafe.config.ConfigFactory
import net.sigusr.mqtt.api._

class MQTTSubscriberProxy extends Actor {



  import net.ceedubs.ficus.Ficus._
  val MQTTPORT = ConfigFactory.load.as[Int]("mqtt.port")
  val MQTTHOST = ConfigFactory.load.as[String]("mqtt.host")

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
