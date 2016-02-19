package com.supersaiyyans.controller

import akka.actor.Props
import com.supersaiyyans.packet.{JsonCmdPacket, JsonCmdPacket$}
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.mvc.{Action, Controller}
import src.main.scala.com.supersaiyyans.actors.DeadPoolSupervisor

object MainController extends Controller{

  val deadpool = Akka.system.actorOf(Props[DeadPoolSupervisor],"DeadpoolSupervisor")



  def serviceCommand= Action(parse.json){
    request=>
      request.body.validate[JsonCmdPacket].map {
        packet=>
          deadpool ! packet
          Ok(packet.toString)
      }.recoverTotal(e=>BadRequest)


  }
}
