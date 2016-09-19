package com.supersaiyyans.actors

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import com.supersaiyyans.actors.ChannelDecider.Write
import com.supersaiyyans.actors.ServiceActors.SupportedChannelTypes.{ChannelType, MQTT}
import com.supersaiyyans.actors.ServicesRepoActor.{AddService, UpdateServiceData}
import com.supersaiyyans.packet._
import com.supersaiyyans.util.Logger
import com.supersaiyyans.actors.CommonMessages.{ServiceData, ServiceState, SwitchServiceState}
import com.supersaiyyans.util.Commons.AssignedServiceId


trait ServiceActor extends Actor with RetryConnect with ChannelDecider {

  val assignedServiceId: AssignedServiceId
  val serviceRepoActor: ActorRef
  val channelType: ChannelType
  val deviceId: String
  val serviceId: String
  val channelActor: ActorRef

  override def preStart(): Unit = {
    Logger.debugWithArgs("Starting new service actor",
      List(
        ("serviceId:", serviceId)
        , ("deviceId", deviceId)
        , ("channelType", channelType.toString)): _*)
  }

}

object SwitchServiceActor {


}

class SwitchServiceActor(override val assignedServiceId: AssignedServiceId, override val deviceId: String, override val serviceId: String
                         , switchData: ServiceData, override val serviceRepoActor: ActorRef, override val channelType: ChannelType)
  extends ServiceActor with ActorLogging {

  def receive = defaultReceive(switchData)

  def defaultReceive(switchServiceData: ServiceData): Receive = LoggingReceive {
    case SwitchServiceState("ON") =>
      val switchServiceState = SwitchServiceState("ON")
      val updatedServiceData: ServiceData = switchData.copy(state = switchServiceState)

      channelActor ! Write(deviceId, serviceId.toInt, "UI", switchServiceState)
      serviceRepoActor ! UpdateServiceData(assignedServiceId, updatedServiceData)
      context.become(defaultReceive(updatedServiceData))

    case SwitchServiceState("OFF") =>
      val switchServiceState = SwitchServiceState("OFF")
      val updatedServiceData: ServiceData = switchData.copy(state = switchServiceState)

      channelActor ! Write(deviceId, serviceId.toInt, "UI", switchServiceState)
      serviceRepoActor ! UpdateServiceData(assignedServiceId, updatedServiceData)
      context.become(defaultReceive(updatedServiceData))

    case SwitchServiceState("TOGGLE") =>

      val (nextState, nextMessage) = if (switchServiceData.state.equals(SwitchServiceState("ON"))) {
        (SwitchServiceState("OFF"), "OFF")
      } else {
        (SwitchServiceState("ON"), "ON")
      }
      val updatedServiceData: ServiceData = switchData.copy(state = nextState)

      channelActor ! Write(deviceId,serviceId.toInt,"UI",nextState)
      serviceRepoActor ! UpdateServiceData(assignedServiceId, updatedServiceData)
      context.become(defaultReceive(updatedServiceData))

    case _ => log.debug("Unexpected message received")

  }

  override def preStart(): Unit = {
    serviceRepoActor ! AddService(assignedServiceId, switchData)
    super.preStart()
  }

}

trait JsonTranformer {
  _: ServiceActor =>
}

trait ChannelDecider {
  _: ServiceActor =>

  override val channelActor = context.actorOf(ChannelDescriber(channelType))
  def ChannelDescriber: (ChannelType) => Props = {
    implicit channel =>
      channel match {
        case _ =>
          Props(new MQTTPubSubProxySupervisorImpl(self, deviceId))
      }
  }
}

object ChannelDecider {

  trait Message

  case class Write(deviceId: String, serviceId: Int, source: String, serviceState: ServiceState) {
    def toBinary = {

    }
  }

}


object ServiceActors {

  sealed trait State

  object Started extends State

  class CommandPacket(val packet: JsonCmdPacket) extends Command

  trait ProtocolDescriber {
    this: Actor =>
    val myProtocol: ChannelType
  }

  trait MQTTActor {
    this: ProtocolDescriber =>
    val myProtocol = MQTT

  }

  object SupportedChannelTypes {

    trait ChannelType

    case object MQTT extends ChannelType

  }

}
