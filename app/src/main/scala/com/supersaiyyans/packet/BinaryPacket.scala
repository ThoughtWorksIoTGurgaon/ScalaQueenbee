package src.main.scala.com.supersaiyyans.packet


trait BinaryPacket {
  def toByteData: Vector[Byte]
}

case class WritePacket(deviceId: String, serviceId: Int, charsData: Array[(Int, Array[Byte])]) extends BinaryPacket {
  override def toByteData: Vector[Byte] = {

    val byteList: List[Vector[Byte]]=
      List(
        1 //version
        ,1 //Packet Type
        ,1,1,1 //Unused Pack
        ,serviceId //ServiceId
        ,charsData.length) //Characteristic Count
        .map(x=>Vector(x.toByte))
              
    val charData: Array[Byte] = charsData.map{
      t =>
        t._1.toByte +: t._2.length.toByte +: t._2
    }.flatten

    val right: Vector[Byte] = charData.tail.foldLeft(Vector(charData.head))((b, a)=>b ++ Vector(a))
    println("Right: ")
    right.map(x=>println(x.toInt.toBinaryString))

    println("ByteList: ")
      byteList.tail.foldLeft(byteList.head)((b , a)=> b ++ a).map(x=>println(x.toInt.toBinaryString))
    println("Z: ")
    val z = byteList.tail.foldLeft(byteList.head)((b , a)=> b ++ a) ++
      right
    z.map(x=>println(x.toInt.toBinaryString))
    z
  }

}
