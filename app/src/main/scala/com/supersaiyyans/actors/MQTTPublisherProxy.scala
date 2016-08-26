package com.supersaiyyans.actors

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.Actor
import com.supersaiyyans.packet.{WritePacket, JsonCmdPacket}
import com.supersaiyyans.store.ServiceStore
import com.typesafe.config.ConfigFactory
import net.sigusr.mqtt.api.{Connect, Connected, Manager, Publish}
import com.supersaiyyans.util.Logger._



class MQTTPublisherProxy extends Actor {

  import net.ceedubs.ficus.Ficus._


  val MQTTPORT = ConfigFactory.load.as[Int]("mqtt.port")
  val MQTTHOST = ConfigFactory.load.as[String]("mqtt.host")



  debug(s"Building publisher with port: ${MQTTPORT} and host: ${MQTTHOST}")
  val mqttPublisher = context.actorOf(
    Manager.props(new InetSocketAddress(MQTTHOST, MQTTPORT)))
  mqttPublisher ! Connect("SCALA_PUBLISHER_ACTOR")


  def receive = {
    case Connected => println(s"${self.path.name} - Connected to MQTT ")
      context.become(ready)
    case _ =>
      println("Message received before connect! => Discarding")
  }

  def ready: Receive = {
    case packet: JsonCmdPacket =>
      val writePacket: WritePacket = ServiceStore
        .get(packet.getServiceAddress)
        .get
        .processAndChangeState(packet)
      debug(s"Sending packet ${writePacket} to device id: ${writePacket.deviceId}")
      mqttPublisher ! Publish("/device/" + writePacket.deviceId + "/cmd", writePacket.toByteData)
    case _ => debug(s"${self.path.name} - Ready now")
  }
}