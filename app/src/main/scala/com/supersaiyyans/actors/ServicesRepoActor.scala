package com.supersaiyyans.actors

import akka.actor.{ActorLogging, FSM, Props}
import com.supersaiyyans.actors.ServicesRepoActor._
import com.supersaiyyans.actors.CommonMessages.ServiceData
import com.supersaiyyans.util.Commons.AssignedServiceId

class ServicesRepoActor extends FSM[State, Data] with ActorLogging {

  startWith(Running, ServicesData(Map.empty))

  // TODO - add exception for unexpected messages

  when(Running) {
    case Event(command: UpdateServiceData, oldData: ServicesData) =>
      val newData =
        oldData
          .data
          .updated(command.assignedServiceId, command.serviceData)
      stay using ServicesData(newData)

    case Event(FetchAll, currentState: ServicesData) =>
      sender ! currentState.data.values.toList
      stay

    case Event(fetchData: FetchServiceData, currentState: ServicesData) =>
      sender ! currentState.data.get(fetchData.assignedServiceId)
      stay

    case Event(newService: AddService, oldData: ServicesData) =>
      val newData = oldData.data + ((newService.assignedServiceId, newService.serviceData))
      stay using ServicesData(newData)

    case Event(CommandPacket(serviceData), services: ServicesData) =>
      stay

    case _ => log.error("Unexpected message found")
      sender ! UnexpectedMessage
      stay
  }

}

object ServicesRepoActor {

  def props = Props[ServicesRepoActor]

  sealed trait State

  case object Initializing extends State

  case object Running extends State

  sealed trait Data

  sealed case class ServicesData(data: Map[AssignedServiceId, ServiceData]) extends Data


  sealed trait SupportedEvent

  case object FetchAll extends SupportedEvent

  case class AddService(assignedServiceId: AssignedServiceId, serviceData: ServiceData) extends SupportedEvent

  case class UpdateServiceData(assignedServiceId: AssignedServiceId, serviceData: ServiceData) extends SupportedEvent

  case class FetchServiceData(assignedServiceId: AssignedServiceId) extends SupportedEvent

  case class CommandPacket(serviceData: ServiceData) extends SupportedEvent

  case object UnexpectedMessage

  // Todo : Make it more specific


}
