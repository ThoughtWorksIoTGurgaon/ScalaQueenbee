package com.supersaiyyans.actors

import com.supersaiyyans.actors.ServicesRepoActor.Running
import org.scalatest.FunSuite
import akka.testkit.TestFSMRef
import akka.actor.{ActorSystem, FSM}
import scala.concurrent.duration._

class ServicesRepoActorTest extends FunSuite {

  implicit val actorSystem = ActorSystem()

  val serviceRepoActorRef = TestFSMRef(new ServicesRepoActor)

  assert(serviceRepoActorRef.stateName  == Running)

}
