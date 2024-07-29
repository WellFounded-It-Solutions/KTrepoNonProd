package se.infomaker.iap.push.huawei

import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import se.infomaker.frt.remotenotification.RemoteNotification
import se.infomaker.frt.remotenotification.RemoteNotificationRouter
import se.infomaker.iap.push.huawei.token.TokenManager
import javax.inject.Inject

@AndroidEntryPoint
class MessageListenerService : HmsMessageService() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var notificationRouter: RemoteNotificationRouter

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        val remoteNotification = RemoteNotification(remoteMessage?.from, remoteMessage?.dataOfMap)
        notificationRouter.route(remoteNotification)
    }

    override fun onNewToken(token: String?) {
        if (!token.isNullOrEmpty()) {
            tokenManager.onToken(token)
        }
    }
}