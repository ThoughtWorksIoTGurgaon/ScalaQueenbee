package com.supersaiyyans.actors

import akka.actor.{ActorRef, Actor, Props}

class TheEnchantressSupervisor(repoActor: ActorRef) extends Actor{

  val mqttDiscoveryActor = context.actorOf(MQTTDiscoveryActor.props(repoActor))
  val theEncantressActor = context.actorOf(TheEnchantress.props(repoActor, List(mqttDiscoveryActor)),"Enchantressssss")

  def receive = {
    case allMessages@_ => theEncantressActor forward allMessages
  }

}

object TheEnchantressSupervisor {
  def props(repoActor: ActorRef) = Props(new TheEnchantressSupervisor(repoActor))
}
