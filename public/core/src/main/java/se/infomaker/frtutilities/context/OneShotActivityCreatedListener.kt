package se.infomaker.frtutilities.context

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

object OneShotActivityCreatedListener {

    @JvmStatic
    fun add(context: Context, action: (Activity) -> Boolean) {
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (action(activity)) {
                    activity.application.unregisterActivityLifecycleCallbacks(this)
                }
            }

            override fun onActivityStarted(activity: Activity) {
                // NOP
            }

            override fun onActivityResumed(activity: Activity) {
                // NOP
            }

            override fun onActivityPaused(activity: Activity) {
                // NOP
            }

            override fun onActivityStopped(activity: Activity) {
                // NOP
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // NOP
            }

            override fun onActivityDestroyed(activity: Activity) {
                // NOP
            }

        })
    }
}