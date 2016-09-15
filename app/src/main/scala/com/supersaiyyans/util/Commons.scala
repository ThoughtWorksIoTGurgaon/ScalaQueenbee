package src.main.scala.com.supersaiyyans.util

import java.util.UUID

object Commons {


  type AssignedServiceId = Int

  object ServiceProfiles extends Enumeration {
    type ServiceProfiles = Value
    val DeviceProfile,SwitchProfile = Value
  }
}
