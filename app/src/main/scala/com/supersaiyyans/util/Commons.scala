package com.supersaiyyans.util

object Commons {


  type AssignedServiceId = Int

  object ServiceProfiles extends Enumeration {
    type ServiceProfiles = Value
    val DeviceProfile,SwitchProfile = Value
  }
}
