package com.supersaiyyans.actors

import akka.actor.{ActorRef, Actor, Props}

class TheEnchantressSupervisor(repoActor: ActorRef) extends Actor{

  val theEncantressActor = context.actorOf(TheEnchantress.props(repoActor))

  def receive = {
    case allMessages@_ => theEncantressActor forward allMessages
  }

  def props = Props[TheEnchantressSupervisor]

}
