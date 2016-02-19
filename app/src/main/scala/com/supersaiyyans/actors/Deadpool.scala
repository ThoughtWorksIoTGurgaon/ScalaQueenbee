package src.main.scala.com.supersaiyyans.actors

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.Actor
import com.supersaiyyans.packet.JsonCmdPacket
import net.sigusr.mqtt.api.{Connect, Connected, Manager, Publish}
import src.main.scala.com.supersaiyyans.packet.WritePacket
import src.main.scala.com.supersaiyyans.service.{Service, SwitchService}


class DeadPool extends Actor {

  val MQTTPORT = 1883
  val MQTTHOST = "192.168.43.11"

  val serviceMap = Map[String, Service](
    "my-device-id:1" -> new SwitchService
  )

  val mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(InetAddress.getByName(MQTTHOST), MQTTPORT)))
  mqttManager ! Connect("SCALA_DISCOVERY_ACTOR")


  def receive = {
    case Connected => println("Connected yo! from:" + sender)
      context.become(ready)
    case _ => println("Got something yadda")
  }

  def ready: Receive = {
    case packet: JsonCmdPacket =>
      val writePacket: WritePacket = serviceMap
        .get(packet.getServiceAddress())
        .get
        .process(packet)

      println("Message Aagaya" + writePacket)
      mqttManager ! Publish("/device/" + writePacket.deviceId + "/cmd", writePacket.toByteData)
    case _ => println("Ready now")
  }
}