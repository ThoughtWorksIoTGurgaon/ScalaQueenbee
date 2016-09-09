package com.supersaiyyans.actors

import akka.actor.Actor.Receive
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.supersaiyyans.actors.MQTTDiscoveryActor.WhichProtocol
import com.supersaiyyans.actors.ProfileType.ProfileType
import com.supersaiyyans.actors.ServiceActors.SupportedChannelTypes.ChannelType
import com.supersaiyyans.actors.ServiceActors.SwitchServiceData
import com.supersaiyyans.actors.TheEnchantress._
import com.supersaiyyans.packet.Packet
import com.supersaiyyans.util.Logger._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._


/*
TODO: Support multi service discovery
 */
class TheEnchantress(repoActor: ActorRef) extends FSM[State,Data] {




  startWith(Processing, EnchantressData(List.empty[ActorRef]))

  when(Processing) {
    case Event(service: ServiceDiscovered, data: EnchantressData) =>
      implicit val askTimeout = Timeout(1 minute)
      debug(s"${self.path.name}: New Service Discovered!!!!")
      debug(s"New Service discovered with profile id: ${service.profileId}")
      sender.ask(WhichProtocol).mapTo[ChannelType].map {
        protocol =>
          val switchServiceActor =
            ServiceActorDecider(ProfileType.SwitchProfile, service.deviceId, service.serviceId, repoActor, protocol)(context)
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

  sealed case class EnchantressData(serviceActors: Seq[ActorRef]) extends Data

  case class ServiceDiscovered(deviceId: String, serviceId: String, profileId: String)


  case class ProcessPacketFromUser(packet: Packet)

  case class ProcessSystemPacket(packet: Packet)

  def props(repoActor: ActorRef) = Props(new TheEnchantress(repoActor))

  trait ServiceState

  case class SwitchServiceState(value: String)

  case class AddServiceActor(serviceActor: ActorRef)

  case class ServiceData(name: String, val serviceId: String, deviceId: String, state: ServiceState)


  def ServiceActorDecider(profileId: ProfileType, deviceId: String, serviceId: String, repoActor: ActorRef, protocolType: ChannelType)
                         (context: ActorContext) = {

    profileId match {
      case ProfileType.SwitchProfile =>
        context
          .actorOf(
            Props(new SwitchServiceActor(deviceId
              , serviceId
              , SwitchServiceData("OFF")
              , repoActor
              , protocolType)))
    }
  }


}

object MQTTUtils {


}

object ProfileType extends Enumeration {
  type ProfileType = Value
  val DiscoveryProfile, SwitchProfile = Value
}
