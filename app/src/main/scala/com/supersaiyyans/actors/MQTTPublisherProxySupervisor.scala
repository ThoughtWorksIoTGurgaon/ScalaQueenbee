package src.main.scala.com.supersaiyyans.actors

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Props, OneForOneStrategy, Actor}
import scala.concurrent.duration._

class MQTTPublisherProxySupervisor extends Actor{

  implicit val timeout = akka.util.Timeout(1 minute)
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10,withinTimeRange = 10 minutes){
      case e:Exception =>
          println(e)
        Restart
    }

  val mqttPublisherProxy = this.context.actorOf(Props[MQTTPublisherProxy],"MQTTPublisherProxy")
  def receive = {
    case x@_ => mqttPublisherProxy ! x
  }

}
