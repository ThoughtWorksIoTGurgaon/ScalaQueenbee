package src.main.scala.com.supersaiyyans.actors

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.Actor
import com.supersaiyyans.packet.JsonCmdPacket
import net.sigusr.mqtt.api.{Connect, Connected, Manager, Publish}
import src.main.scala.com.supersaiyyans.packet.WritePacket
import src.main.scala.com.supersaiyyans.store.ServiceStore


class MQTTPublisherProxy extends Actor {

  val MQTTPORT = 1883
  val MQTTHOST = "192.168.43.11"

  val mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(InetAddress.getByName(MQTTHOST), MQTTPORT)))
  mqttManager ! Connect("SCALA_PUBLISHER_ACTOR")


  def receive = {
    case Connected => println(s"${self.path.name} - Connected to MQTT ")
      context.become(ready)
    case _ =>
      println("Message received before connect! => Discarding")
  }

  def ready: Receive = {
    case packet: JsonCmdPacket =>
      val writePacket: WritePacket = ServiceStore
        .get(packet.getServiceAddress()).get
        .process(packet)
      println(s"Sending packet ${writePacket} to device id: ${writePacket.deviceId}")
      mqttManager ! Publish("/device/" + writePacket.deviceId + "/cmd", writePacket.toByteData)
    case _ => println(s"${self.path.name} - Ready now")
  }
}