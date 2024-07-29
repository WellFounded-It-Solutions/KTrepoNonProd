package se.infomaker.iap.provisioning

import android.content.Context
import android.content.Intent
import se.infomaker.frt.remotenotification.RemoteNotificationManager
import se.infomaker.frt.statistics.UserInfomationHandler
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.ActionManager
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.iap.action.display.DisplayManager
import se.infomaker.iap.provisioning.action.LogoutActionHandler
import se.infomaker.iap.provisioning.notification.ValidProductInterceptor
import se.infomaker.iap.provisioning.ui.PaywallActivity

class Setup : AbstractInitContentProvider() {
    override fun init(context: Context) {
        val provisioningManager = ProvisioningManagerProvider.provide(context)
        provisioningManager.loginManager()?.let {
            ActionManager.register("logout", LogoutActionHandler(it))
            DisplayManager.register("display-login", DisplayLoginHandler())
            ActionManager.register("display-create-account", object: ActionHandler{
                override fun canPerform(context: Context, operation: Operation): Boolean {
                    return true
                }

                override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
                    val intent = Intent(context, PaywallActivity::class.java)
                    intent.putExtra("restartAppOnGoToContent", false)
                    intent.putExtra("showOptOutButton", false)

                    context.startActivity(intent)
                }
            })
            UserInfomationHandler.provider = it
        }
        RemoteNotificationManager.registerInterceptor(ValidProductInterceptor(provisioningManager))
        ForegroundTracker.attach(context)
    }
}