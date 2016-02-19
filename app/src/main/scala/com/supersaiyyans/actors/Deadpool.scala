package src.main.scala.com.supersaiyyans.actors

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.Actor
import net.sigusr.mqtt.api.{Connect, Connected, Manager}


class DeadPool extends Actor{

  val MQTTPORT = 1883
  val MQTTHOST = "localhost"

  val mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(InetAddress.getByName(MQTTHOST), MQTTPORT)))
  mqttManager ! Connect("SCALA_DISCOVERY_ACTOR")


  def receive = {
    case Connected => println("Connected yo! from:" + sender)
      context.become(ready)
    case _ => println("Got something yadda")
  }

  def ready: Receive = {
    case _ => println("Ready now")
  }
}
