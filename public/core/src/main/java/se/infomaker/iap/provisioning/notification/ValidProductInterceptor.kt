package se.infomaker.iap.provisioning.notification

import se.infomaker.frt.remotenotification.AbortNotificationException
import se.infomaker.frt.remotenotification.RemoteNotification
import se.infomaker.frt.remotenotification.RemoteNotificationInterceptor
import se.infomaker.iap.provisioning.ProvisioningManager
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ValidProductInterceptor(private val provisioningManager: ProvisioningManager): RemoteNotificationInterceptor {

    override fun intercept(remoteNotification: RemoteNotification): RemoteNotification {
        if (!provisioningManager.hasAppStartPaywall()) {
            return remoteNotification
        }
        val latch = CountDownLatch(1)
        var result = false
        provisioningManager.checkPermissionToPassPaywall({
            result = it
            latch.countDown()

        }, {
            Timber.e(it, "Failed to validate paywall permission")
            latch.countDown()
        })
        latch.await(60, TimeUnit.SECONDS)
        if (!result) {
            throw AbortNotificationException("The user does not have permission to pass paywall")
        }
        return remoteNotification
    }
}