package com.supersaiyyans.packet


trait Packet {
  val toByteData: Vector[Byte]
  val getServiceAddress: String
  val getSourceAddress: String
  val getDestinationAddress: String
}

trait BinaryPacket extends Packet

case class WritePacket(deviceId: String, serviceId: Int, payload: Array[(Int, Array[Byte])], source: String) extends BinaryPacket {

  override val toByteData: Vector[Byte] = {
    (transformHeader ++ transformPayload).toVector
  }

  override val getServiceAddress = serviceId.toString

  lazy val transformPayload: Array[Byte] = {
    payload.flatMap {
      case (characteristic, data) =>
        characteristic.toByte +:
          data.length.toByte +:
          data
    }
  }

  lazy val transformHeader: Seq[Byte] = {
    Seq(
      1 //version
      , 1 //Packet Type
      , 1, 1, 1 //Unused Pack
      , serviceId //ServiceId
      , payload.length) //Characteristic Count
      .map(x => x.toByte)
  }

  override val getSourceAddress: String = source
  override val getDestinationAddress: String = ???
}
