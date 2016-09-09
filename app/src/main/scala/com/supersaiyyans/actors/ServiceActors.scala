package com.supersaiyyans.actors

import akka.actor.{Actor, ActorRef, FSM, Props}
import com.supersaiyyans.actors.ServiceActors.SupportedChannelTypes.{MQTT, ChannelType}
import com.supersaiyyans.actors.ServiceActors._
import com.supersaiyyans.packet._
import com.supersaiyyans.util.Logger._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._


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

//
//object SwitchServiceActor {
//
//  def props(deviceId: String, serviceId: String, switchData: SwitchServiceData, serviceRepoActor: ActorRef, protocolType: ChannelType) = {
//    Props(new SwitchServiceActor(deviceId, serviceId, switchData, serviceRepoActor, protocolType) with ChannelDecider)
//  }
//}


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
