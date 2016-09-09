package com.supersaiyyans.actors

import akka.actor.{Actor, ActorRef, Props}
import com.supersaiyyans.actors.ServiceActors.SupportedChannelTypes.{ChannelType, MQTT}
import com.supersaiyyans.packet._
import src.main.scala.com.supersaiyyans.actors.CommonMessages.SwitchServiceData


class SwitchServiceActor(override val deviceId: String, override val serviceId: String
                                  , switchData: SwitchServiceData, override val serviceRepoActor: ActorRef, override val channelType: ChannelType)
  extends ServiceActor{

  def receive = {
    case _ =>
  }

}

trait JsonTranformer {
  _: ServiceActor =>
}

trait ServiceActor extends Actor with RetryConnect with ChannelDecider{
  val serviceRepoActor: ActorRef
  val channelType: ChannelType
  val deviceId: String
  val serviceId: String
  val channelActor: ActorRef

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


object ServiceActors {

  sealed trait State

  object Started extends State

  sealed trait Data

  sealed trait ServiceData extends Data

  case class SwitchServiceData(val value: String) extends ServiceData

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
