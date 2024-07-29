package se.infomaker.livecontentui.livecontentrecyclerview.notification

import se.infomaker.frt.remotenotification.NotificationFilter
import se.infomaker.frt.remotenotification.RemoteNotification
import timber.log.Timber

class ContentListNotificationFilter(private val moduleIdentifier: String): NotificationFilter {
    override fun matches(notification: RemoteNotification): Boolean {
        val data = notification.data
        if (data == null) {
            Timber.d("Ignoring notification without data")
            return false
        }
        data["context"]?.let {
            if (moduleIdentifier != it) {
                return false
            }
        }
        val noText = data["message"].isNullOrEmpty() && data["title"].isNullOrEmpty()
        if (noText || data["uuid"].isNullOrEmpty()) {
            Timber.d("Ignoring notification no message field or uuid")
            return false
        }
        return true
    }
}