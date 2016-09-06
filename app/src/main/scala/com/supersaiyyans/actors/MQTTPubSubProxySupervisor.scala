package com.supersaiyyans.actors

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import com.supersaiyyans.actors.DeviceProxySupervisor.MessageReceivedFromDevice
import com.supersaiyyans.util.Logger._
import com.typesafe.config.ConfigFactory
import net.sigusr.mqtt.api._

abstract class MQTTPubSubProxySupervisor(override val deviceListener: ActorRef, deviceId: String)
  extends DeviceProxySupervisor {

  val subscribeTopic = s"/device/${deviceId}/data"

  override def onDeviceConnect = {

    case Event(Connected, data: Any) =>
      debug(s"${self.path.name} - Connected to MQTT ")
      data
  }

  override def onDeviceDisconnect = {
    case Event(Disconnect, data: Any) =>
      data
  }

  override def onMessageReceivedFromDevice = {
    case Event(msg: Message, stateData) =>
      debug(s"Message Receved: ${msg.payload}")
      MessageReceivedFromDevice(msg.payload)
  }

}

class MQTTPubSubProxySupervisorImpl(override val deviceListener: ActorRef, deviceId: String)
  extends MQTTPubSubProxySupervisor(deviceListener, deviceId) {

  import net.ceedubs.ficus.Ficus._

  val MQTTPORT = ConfigFactory.load.as[Int]("mqtt.port")
  val MQTTHOST = ConfigFactory.load.as[String]("mqtt.host")
  debug(s"Building publisher with port: ${MQTTPORT} and host: ${MQTTHOST}")

  override val deviceProxy = context.actorOf(
    Manager.props(new InetSocketAddress(MQTTHOST, MQTTPORT)))

  deviceProxy ! Connect("SCALA_PUB_SUB_ACTOR - " + self.path.name)
  deviceProxy ! Subscribe(Vector((subscribeTopic, AtMostOnce)), MessageId(10))


}

object MQTTPubSubProxySupervisor {


}
