package src.main.scala.com.supersaiyyans.packet


trait BinaryPacket {
  def toByteData: Vector[Byte]
}

case class WritePacket(deviceId: String, serviceId: Int, charsData: Array[(Int, Array[Byte])]) extends BinaryPacket {
  override def toByteData: Vector[Byte] = {

    val byteList: List[Vector[Byte]]= List(1,1,1,1,1,serviceId,charsData.length).map(x=>Vector(x.toByte))
              

    //    val something: Vector[Byte] = Vector(
    //      1 toByte, 1 toByte, 0 toByte, 0 toByte, 0 toByte,
    //      serviceId.toByte,
    //    1 toByte,
    //    charsData.length.toByte
    //    )

    val charData: Array[Byte] = charsData.map{
      t =>
        t._1.toByte +: t._2.length.toByte +: t._2
    }.flatten

    val right: Vector[Byte] = charData.tail.foldLeft(Vector(charData.head))((b, a)=>b ++ Vector(a))
    val z = byteList
      .tail
      .foldLeft(byteList.head)((b , a)=> a ++ b) ++
      right
    z.map(x=>println(x.toInt.toBinaryString))
    z
  }

}

object WritePacket;
