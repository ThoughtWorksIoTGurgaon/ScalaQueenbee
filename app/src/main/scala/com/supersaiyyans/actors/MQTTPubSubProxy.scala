package src.main.scala.com.supersaiyyans.actors

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.Actor
import com.supersaiyyans.packet.{WritePacket, JsonCmdPacket}
import com.supersaiyyans.service.DiscoveryService
import com.supersaiyyans.store.ServiceStore
import com.supersaiyyans.util.Logger._
import com.typesafe.config.ConfigFactory
import net.sigusr.mqtt.api._


//TODO: Queued Message Buffer
class MQTTPubSubProxy extends Actor with RetryConnect{

  import net.ceedubs.ficus.Ficus._

  val MQTTPORT = ConfigFactory.load.as[Int]("mqtt.port")
  val MQTTHOST = ConfigFactory.load.as[String]("mqtt.host")
  var queuedMessagesBuffer = Seq.empty
  val subscribeTopic = "/device/+/data"


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

object MQTTPubSubProxy {

  sealed trait DATA

  sealed trait STATE
}