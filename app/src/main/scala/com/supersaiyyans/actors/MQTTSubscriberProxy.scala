package com.supersaiyyans.actors

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.Actor
import com.supersaiyyans.service.DiscoveryService
import com.typesafe.config.ConfigFactory
import net.sigusr.mqtt.api._
import com.supersaiyyans.util.Logger._

class MQTTSubscriberProxy extends Actor {

  import net.ceedubs.ficus.Ficus._
  val MQTTPORT = ConfigFactory.load.as[Int]("mqtt.port")
  val MQTTHOST = ConfigFactory.load.as[String]("mqtt.host")
  var queuedMessagesBuffer = Seq.empty
  val subscribeTopic = "/device/+/data"
  val mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(InetAddress.getByName(MQTTHOST), MQTTPORT)))

  debugWithArgs("Starting subscriber with args",Seq("MQTTHOST"->MQTTHOST,"MQTTPORT"->MQTTPORT.toString): _*)
  mqttManager ! Connect("SCALA_DISCOVERY_ACTOR")


  def receive = initialState

  def initialState: Receive = {
    case Connected =>
      debug("-------------------Discovery Actor Connected--------------------")
      mqttManager ! Subscribe(Vector((subscribeTopic, AtMostOnce)), MessageId(10))
      context become ready

  }

  def ready: Receive = {
    case Message(topic, byteVector) =>
      val TopicDeviceExtractor = "(\\/device\\/)([a-z|-]*)(\\/data)".r
      debug(s"Topic - ${topic}")
      topic match {
        case TopicDeviceExtractor(_, deviceId: String, _) =>
          debug("Received a message from device: " + deviceId)
          DiscoveryService.process(deviceId, byteVector)
        case _ => debug("Unknown message received")
      }
      debug("Received: " + byteVector)
    case x@_ =>
      debug(s"Unknown message received:${x}")
  }

}
