package com.supersaiyyans.packet

import org.scalatest.FunSuite

class WritePacketTest extends FunSuite {

  //Vector(1, 1, 1, 1, 1, -24, 1, 1, 1, 2)
  test("should convert write packet to a byte vector") {
    val writePacket = WritePacket("myDeviceId", 127, Array(Tuple2(1,Array(2.toByte))),"")
    println{
      writePacket.transformHeader
    }
    assertResult(Vector(1, 1, 1, 1, 1, -24, 1, 1, 1, 2))(writePacket.toByteData)
  }

}
