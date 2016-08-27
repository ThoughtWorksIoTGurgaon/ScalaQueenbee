package com.supersaiyyans.actors

import akka.actor.{ActorRef, Actor, Props}

class TheEnchantressSupervisor(repoActor: ActorRef) extends Actor{

  val theEncantressActor = context.actorOf(TheEnchantress.props(repoActor),"Enchantressssss")

  def receive = {
    case allMessages@_ => theEncantressActor forward allMessages
  }

}

object TheEnchantressSupervisor {
  def props(repoActor: ActorRef) = Props(new TheEnchantressSupervisor(repoActor))
}
