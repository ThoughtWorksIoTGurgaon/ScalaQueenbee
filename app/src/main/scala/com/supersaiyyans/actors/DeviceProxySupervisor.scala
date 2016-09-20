package com.supersaiyyans.actors

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import com.supersaiyyans.actors.DeviceProxySupervisor._

import scala.concurrent.duration._


abstract class DeviceProxySupervisor extends FSM[State, Data] {

  val deviceListener: ActorRef
  implicit val timeout = akka.util.Timeout(1 minute)
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 minutes) {
      case e: Exception =>
        println(e)
        Restart
    }

  startWith(Disconnected, QueuedMessages(Nil))

  def onDeviceConnect: PartialFunction[Event, Data]

  def onDeviceDisconnect: PartialFunction[Event, Data]

  def onMessageToDevice: PartialFunction[Event, Data]

  def onMessageReceivedFromDevice: PartialFunction[Event, MessageReceivedFromDevice]


  val deviceProxy: ActorRef

  when(Disconnected) {
    onDeviceConnect.andThen {
      data =>
        goto(Connected).using(data)
    } orElse {
      case Event(someMessage, QueuedMessages(messages)) =>
        stay using QueuedMessages(messages :+ someMessage)
    }
  }

  when(Connected) {
    onDeviceDisconnect.andThen { data =>
      goto(Disconnected)
    } orElse {
      onMessageReceivedFromDevice.andThen {
        messageFromDevice =>
          deviceListener ! messageFromDevice
          stay.using(QueuedMessages(Nil))
      } orElse {
        onMessageToDevice.andThen {
          state =>
            stay.using(QueuedMessages(Nil))
        }
      } orElse {
        case Event(someMessage: Any, _: Any) =>
          stay using QueuedMessages(Nil)
      }
    }
  }


  onTransition {
    case Disconnected -> Connected =>
      stateData match {
        case QueuedMessages(messages) =>
          messages.foreach {
            self ! _
          }
      }

  }

}


object DeviceProxySupervisor {

  sealed trait State

  case object AwaitingConnect extends State

  case object Connected extends State

  case object Disconnected extends State

  case object Reconnecting extends State

  sealed trait Data

  private final case class QueuedMessages(message: List[Any]) extends Data

  final case class MessageReceivedFromDevice(message: Any)

}
