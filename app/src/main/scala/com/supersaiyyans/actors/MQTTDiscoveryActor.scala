package com.supersaiyyans.actors

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}
import com.supersaiyyans.actors.MQTTDiscoveryActor.WhichProtocol
import com.supersaiyyans.actors.ServiceActors.SupportedChannelTypes.{ChannelType, MQTT}
import com.supersaiyyans.actors.ServiceActors.{MQTTActor, ProtocolDescriber, SupportedChannelTypes}
import com.supersaiyyans.actors.TheEnchantress.ServiceDiscovered
import com.supersaiyyans.util.Logger._
import com.typesafe.config.ConfigFactory
import net.sigusr.mqtt.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/*
TODO:
1.Discovery with Profiles
2.ReadPacket for NewlyDiscovered Services
3.Supervisor Strategy
 */

class MQTTDiscoveryActor(override val serviceRepoActor: ActorRef) extends ServiceActor with ProtocolDescriber with MQTTActor {

  import net.ceedubs.ficus.Ficus._

  val MQTTPORT = ConfigFactory.load.as[Int]("mqtt.port")
  val MQTTHOST = ConfigFactory.load.as[String]("mqtt.host")
  var queuedMessagesBuffer = Seq.empty
  val subscribeTopic = "/device/+/data"
  val mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(InetAddress.getByName(MQTTHOST), MQTTPORT)))
  val enchantress = context.parent
  val ServiceCountByteIndex = 9
  val scheduler = context.system.scheduler


  debugWithArgs("Starting MQTT Discovery Actor with args", Seq("MQTTHOST" -> MQTTHOST, "MQTTPORT" -> MQTTPORT.toString): _*)
  scheduler.scheduleOnce(1 minutes, self, TryConnect)


  type ProfileId = String
  type ServiceId = String
  type ServiceInfo = (ProfileId, ServiceId)

  def extractServiceInfoList(byteVector: Vector[Byte]): List[ServiceInfo] = {
    val serviceCount: Byte = byteVector(ServiceCountByteIndex)
    val services = customZip2(byteVector.toList.drop(ServiceCountByteIndex + 1))
    services
  }

  def customZip2(byteVector: List[Byte]): List[ServiceInfo] = {
    def customZip2(byteVector: List[Byte], acc: List[ServiceInfo]): List[ServiceInfo] = {
      byteVector match {
        case Nil => acc
        case x :: Nil => acc
        case x :: y :: xs => customZip2(xs, acc :+(x.toInt.toString, y.toInt.toString))

      }
    }
    customZip2(byteVector, Nil)
  }

  def receive = initializing

  def initializing: Receive = {

    case TryConnect =>
      debug("-----------------RECEIVED TryConnect--------------")
      mqttManager ! Connect("QUEENBEE_MQTT_DISCOVERY_ACTOR_CONNECTING")
      scheduler.scheduleOnce(5 minutes, self, TryConnect)
    case Connected =>
      debug("-------------------Discovery Actor Connected--------------------")
      mqttManager ! Subscribe(Vector((subscribeTopic, AtMostOnce)), MessageId(10))
      context become ready
  }

  def ready: Receive = {
    case Disconnected | ConnectionFailure =>
      scheduler.scheduleOnce(5 minutes, self, TryConnect)
      context become initializing

    case Message(topic, byteVector) =>
      val TopicDeviceExtractor = "(\\/device\\/)([a-z|-]*)(\\/data)".r
      debug(s"Topic - ${topic}")
      topic match {
        case TopicDeviceExtractor(_, deviceId: String, _) =>
          debug("Received a message from device: " + deviceId)
          extractServiceInfoList(byteVector).foreach {
            case (profileId, serviceId) =>
              enchantress ! ServiceDiscovered(deviceId, serviceId, profileId)
          }
        case _ => debug("Unknown message received")
      }
      debug("Received byteVector:  " + byteVector)
    case WhichProtocol =>
      sender ! myProtocol
    case x@_ =>
      debug(s"Unknown message received:${x}")
  }


  override val serviceId: String = "0"
  override val channelType: ChannelType = MQTT
  override val deviceId: String = "0"
}

object MQTTDiscoveryActor {
  object WhichProtocol
  def props = Props[MQTTDiscoveryActor]
}



trait RetryConnect {
  this: {
    def receive: Receive => Unit
  } =>

  object TryConnect

}
