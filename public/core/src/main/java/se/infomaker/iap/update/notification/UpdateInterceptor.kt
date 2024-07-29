package se.infomaker.iap.update.notification

import se.infomaker.frt.remotenotification.AbortNotificationException
import se.infomaker.frt.remotenotification.RemoteNotification
import se.infomaker.frt.remotenotification.RemoteNotificationInterceptor
import se.infomaker.iap.update.UpdateManager
import se.infomaker.iap.update.UpdateType
import javax.inject.Inject

class UpdateInterceptor @Inject constructor(
    private val updateManager: UpdateManager
) : RemoteNotificationInterceptor {

    override fun intercept(remoteNotification: RemoteNotification) = when (updateManager.get().type) {
        UpdateType.OBSOLETE -> throw AbortNotificationException("The app is obsolete, will not receive any updates and is unusable.")
        else -> remoteNotification
    }
}