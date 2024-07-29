package se.infomaker.livecontentui.livecontentdetailview.frequency

import se.infomaker.livecontentui.config.MeteredAccess

data class Frequency(val uuid:String, val permission:String, val created:Long, val property:String?, val meteredAccess: MeteredAccess?)