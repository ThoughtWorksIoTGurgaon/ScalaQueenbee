package src.main.scala.com.supersaiyyans.util

import java.util.UUID

object Commons {


  type AssignedServiceId = UUID

  object ServiceProfiles extends Enumeration {
    type ServiceProfiles = Value
    val DeviceProfile,SwitchProfile = Value
  }
}
