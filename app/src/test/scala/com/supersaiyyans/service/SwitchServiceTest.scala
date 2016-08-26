package com.supersaiyyans.service

import com.supersaiyyans.packet.JsonCmdPacket
import com.supersaiyyans.service.SwitchService.SwitchServiceState
import org.scalatest.FunSuite

class SwitchServiceTest extends FunSuite {

  val switchServiceState = SwitchServiceState("ON")
  val serviceAddress = "myDevice:1000"
  val switchService = SwitchService(serviceAddress, switchServiceState)

  test("convert switch service to json") {
    val switchServiceJson = switchService.toJson()

    assertResult("myDevice:1000")((switchServiceJson \ "serviceAddress").as[String])
    assertResult("ON")((switchServiceJson \ "state").as[String])
    assertResult("SWH")((switchServiceJson \ "serviceType").as[String])
  }

  test("process a json cmd packet") {
    val jsonCmdPacket = JsonCmdPacket("myDevice:1000", "OFF")
    val writePacket = switchService.processAndChangeState(jsonCmdPacket)
    assertResult("OFF")(switchService.state.value)
  }

}
