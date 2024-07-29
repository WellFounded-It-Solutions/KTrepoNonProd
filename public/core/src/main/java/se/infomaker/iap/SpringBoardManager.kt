package se.infomaker.iap

import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import se.infomaker.frtutilities.ktx.findActivity


object SpringBoardManager : StateRouter {

    var router: StateRouter? = null

    override fun route(context: Context, intent: Intent, onComplete: () -> Unit, onCancel: () -> Unit) {
        if (router != null) {
            router?.route(context, intent, onComplete, onCancel)
        }
        else {
            throw RuntimeException("No state router defined")
        }
    }

    override fun currentRoute() = router?.currentRoute()

    fun restart(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        context.findActivity()?.let { activity ->
            ActivityCompat.finishAffinity(activity)
        }
        context.startActivity(Intent.makeRestartActivityTask(componentName))
    }
}