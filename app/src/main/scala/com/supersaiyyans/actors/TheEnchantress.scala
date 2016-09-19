package com.supersaiyyans.actors

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.supersaiyyans.actors.MQTTDiscoveryActor.WhichProtocol
import com.supersaiyyans.actors.ProfileType.ProfileType
import com.supersaiyyans.actors.ServiceActors.SupportedChannelTypes.ChannelType
import com.supersaiyyans.actors.ServicesRepoActor.CommandPacket
import com.supersaiyyans.actors.TheEnchantress._
import com.supersaiyyans.packet.Packet
import com.supersaiyyans.util.Logger._
import com.supersaiyyans.actors.CommonMessages.{ServiceData, SwitchServiceState}
import com.supersaiyyans.util.Commons.AssignedServiceId

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._


/*
TODO: Support multi service discovery
 */
class TheEnchantress(repoActor: ActorRef, discoveryActors: List[ActorRef]) extends FSM[State, Data] with LoggingFSM[State, Data] {


  startWith(Processing, EnchantressData(Map.empty[AssignedServiceId, ActorRef]))

  when(Processing) {
    case Event(service: DiscoveredService, data: EnchantressData) =>
      implicit val askTimeout = Timeout(1 minute)
      debug(s"${self.path.name}: New Service Discovered!!!!")
      debug(s"New Service discovered with profile id: ${service.profileId}")
      sender.ask(WhichProtocol).mapTo[ChannelType].map {
        protocol =>
          ServiceActorDecider((service.deviceId + service.serviceId).hashCode, ProfileType(service.profileId.toInt)
            , service.deviceId, service.serviceId, repoActor, protocol, data.serviceActors)(context)

      }
      stay

    case Event(AddServiceActor(assignedServiceId, actor), data: EnchantressData) =>
      stay using EnchantressData(
        data.serviceActors +
          ((assignedServiceId, actor)))

    case Event(Terminated(message), data) =>
      debug(s"RepoActor is down,enchantress wont work properly: ${message}")
      stay

    case Event(CommandPacket(serviceData), data: EnchantressData) =>
      stay

    case msg@_ =>
      debug(s"Enchantress received unknown message!")
      stay
  }

  def ServiceActorDecider(assignedServiceId: AssignedServiceId, profileId: ProfileType, deviceId: String, serviceId: String, repoActor: ActorRef, protocolType: ChannelType, mapOfExistingActors: Map[AssignedServiceId, ActorRef])
                         (context: ActorContext) = {
    if(!mapOfExistingActors.keys.exists(_ == assignedServiceId)){
      profileId match {
        case ProfileType.SwitchProfile =>
          self ! AddServiceActor(assignedServiceId,
            context.actorOf(Props(new SwitchServiceActor(
              assignedServiceId
              , deviceId
              , serviceId
              , ServiceData("SWITCH 1", assignedServiceId, SwitchServiceState("OFF"))
              , repoActor
              , protocolType))))
      }
    }
  }


}


object TheEnchantress {

  sealed trait State

  object Processing extends State

  sealed trait Data

  sealed case class EnchantressData(serviceActors: Map[AssignedServiceId, ActorRef]) extends Data

  case class DiscoveredService(deviceId: String, serviceId: String, profileId: String)

  case class ProcessPacketFromUser(packet: Packet)

  case class ProcessSystemPacket(packet: Packet)

  def props(repoActor: ActorRef, discoveryActors: List[ActorRef]) = Props(new TheEnchantress(repoActor, discoveryActors))

  case class AddServiceActor(assignedServiceId: AssignedServiceId, serviceActor: ActorRef)


}

object MQTTUtils {


}

object ProfileType extends Enumeration(initial = 1) {
  type ProfileType = Value
  val DiscoveryProfile, SwitchProfile = Value
}
