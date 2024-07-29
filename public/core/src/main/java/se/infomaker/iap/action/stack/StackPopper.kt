package se.infomaker.iap.action.stack

import android.app.Activity
import se.infomaker.frt.util.AbstractActivityLifecycleCallbackListener
import se.infomaker.iap.action.Result

class StackPopper(private val onResult: (Result) -> Unit) : AbstractActivityLifecycleCallbackListener() {

    fun popToRoot(initialActivity: Activity) {
        if (initialActivity.isTaskRoot) {
            onResult(Result(true))
        }
        else {
            initialActivity.application.registerActivityLifecycleCallbacks(this)
            initialActivity.finish()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity.isTaskRoot) {
            onResult(Result(true))
            activity.application.unregisterActivityLifecycleCallbacks(this)
        }
        else {
            activity.finish()
        }
    }
}