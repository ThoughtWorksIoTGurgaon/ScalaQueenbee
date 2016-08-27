package src.main.scala.com.supersaiyyans.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import com.supersaiyyans.actors.RetryConnect
import com.supersaiyyans.packet.JsonCmdPacket
import com.supersaiyyans.util.Logger._
import com.typesafe.config.ConfigFactory
import net.sigusr.mqtt.api._


//TODO: Queued Message Buffer
class MQTTPubSubProxy(deviceId: String) extends Actor with RetryConnect {

  import net.ceedubs.ficus.Ficus._

  val MQTTPORT = ConfigFactory.load.as[Int]("mqtt.port")
  val MQTTHOST = ConfigFactory.load.as[String]("mqtt.host")
  var queuedMessagesBuffer = Seq.empty
  val subscribeTopic = s"/device/${deviceId}/data"


  debug(s"Building publisher with port: ${MQTTPORT} and host: ${MQTTHOST}")
  val mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(MQTTHOST, MQTTPORT)))
  mqttManager ! Connect("SCALA_PUB_SUB_ACTOR - " + self.path.name)
  mqttManager ! Subscribe(Vector((subscribeTopic, AtMostOnce)), MessageId(10))

  def receive = {
    case Connected => println(s"${self.path.name} - Connected to MQTT ")
      context.become(ready)
    case _ =>
      println("Message received before connect! => Discarding")
  }

  def ready: Receive = {
    case packet: JsonCmdPacket =>
    //      val writePacket: WritePacket = ServiceStore
    //        .get(packet.getServiceAddress())
    //        .get
    //        .processAndChangeState(packet)
    //      debug(s"Sending packet ${writePacket} to device id: ${writePacket.deviceId}")

    //      mqttManager ! Publish("/device/" + writePacket.deviceId + "/cmd", writePacket.toByteData)


    case Message(topic, byteVector) =>
      debug(s"Topic - ${topic}")
      debug("Received a ReadResponse message from device: " + deviceId)
      debug("Received: " + byteVector)
    case x@_ =>
      debug(s"Unknown message received:${x}")
  }

}

object MQTTPubSubProxy {

  def props(deviceId: String) = Props(new MQTTPubSubProxy(deviceId))

}