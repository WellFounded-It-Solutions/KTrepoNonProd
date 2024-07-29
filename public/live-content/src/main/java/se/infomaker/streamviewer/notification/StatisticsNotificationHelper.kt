package se.infomaker.streamviewer.notification

object StatisticsNotificationHelper {
    @JvmStatic
    fun keyToNew(`in`: String): String = when (`in`) {
        "field" -> "streamType"
        "value" -> "streamID"
        else -> `in`
    }
}