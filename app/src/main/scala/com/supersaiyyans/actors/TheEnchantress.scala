package com.supersaiyyans.actors

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.supersaiyyans.actors.MQTTDiscoveryActor.WhichProtocol
import com.supersaiyyans.actors.ProfileType.ProfileType
import com.supersaiyyans.actors.ServiceActors.SwitchServiceData
import com.supersaiyyans.actors.SupportedProtocolTypes._
import com.supersaiyyans.actors.TheEnchantress._
import com.supersaiyyans.packet.Packet
import com.supersaiyyans.util.Logger._
import src.main.scala.com.supersaiyyans.actors.MQTTPubSubProxy

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._


/*
TODO: Support multi service discovery
 */
class TheEnchantress(repoActor: ActorRef) extends FSM[State, Data] {

  context watch repoActor
  startWith(Processing, EnchantressData(List.empty[ActorRef]))

  when(Processing) {
    case Event(service: ServiceDiscovered, data: EnchantressData) =>
      implicit val askTimeout = Timeout(1 minute)
      debug(s"${self.path.name}: New Service Discovered!!!!")
      debug(s"New Service discovered with profile id: ${service.profileId}")
      sender.ask(WhichProtocol).mapTo[ProtocolType].map {
        protocol =>
          val switchServiceActor =
            ServiceActorDecider(ProfileType.SwitchProfile, service.deviceId, service.serviceId, repoActor, protocol)(ProtocolActorDecider)(context)
          self ! AddServiceActor(switchServiceActor)
      }
      stay

    case Event(AddServiceActor(actor), data: EnchantressData) =>
      stay using EnchantressData(data.serviceActors :+ actor)

    case Event(Terminated(message), data) =>
      debug(s"RepoActor is down,enchantress wont work properly: ${message}")
      stay
  }
}


object TheEnchantress {

  sealed trait State

  object Processing extends State

  sealed trait Data

  sealed case class EnchantressData(serviceActors: List[ActorRef]) extends Data

  case class ServiceDiscovered(deviceId: String, serviceId: String, profileId: String)


  case class ProcessPacketFromUser(packet: Packet)

  case class ProcessSystemPacket(packet: Packet)

  def props(repoActor: ActorRef) = Props(new TheEnchantress(repoActor))

  trait ServiceState

  case class SwitchServiceState(value: String)

  case class AddServiceActor(serviceActor: ActorRef)

  case class ServiceData(name: String, val serviceId: String, deviceId: String, state: ServiceState)


  def ServiceActorDecider(profileId: ProfileType, deviceId: String, serviceId: String, repoActor: ActorRef, protocolType: ProtocolType)
                         (protocolActorDecider: (ProtocolType) => (String) => Props)
                         (context: ActorContext) = {

    profileId match {
      case ProfileType.SwitchProfile =>
        val mqttPubSubProxyActor =
          context.actorOf(protocolActorDecider(protocolType)(deviceId))
        context
          .actorOf(
            SwitchServiceActor.props(deviceId
              , serviceId
              , SwitchServiceData("OFF")
              , mqttPubSubProxyActor))
    }
  }

  def ProtocolActorDecider: (ProtocolType) => (String) => Props = {
    protocol =>
      protocol match {
        case _ => MQTTPubSubProxy.props _
      }
  }

}

object ProfileType extends Enumeration {
  type ProfileType = Value
  val DiscoveryProfile, SwitchProfile = Value
}
