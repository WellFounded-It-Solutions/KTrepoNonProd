package se.infomaker.frt.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import se.infomaker.frt.moduleinterface.deeplink.UriTarget
import se.infomaker.frt.moduleinterface.deeplink.UriTargetHandler
import se.infomaker.frt.remotenotification.notification.NotificationIntentFactory
import se.infomaker.iap.SpringBoardActivity

class DeepLinkActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let { uri ->
            se.infomaker.frt.moduleinterface.deeplink.DeepLinkUrlManager.handle(this,object : UriTargetHandler{
                override fun open(target: UriTarget) {
                    val intent = Intent(this@DeepLinkActivity, SpringBoardActivity::class.java)
                    intent.putExtra(NotificationIntentFactory.MODULE_ID, target.moduleId)
                    intent.putExtra(NotificationIntentFactory.NOTIFICATION_DATA, target.data) // TODO Use data instead of notification
                    this@DeepLinkActivity.startActivity(intent)
                    this@DeepLinkActivity.overridePendingTransition(0,0)
                }

            } , uri) {
                finish()
            }
        } ?: kotlin.run {
            finish()
        }
    }
}