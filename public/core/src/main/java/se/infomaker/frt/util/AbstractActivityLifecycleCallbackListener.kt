package se.infomaker.frt.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

abstract class AbstractActivityLifecycleCallbackListener :  Application.ActivityLifecycleCallbacks {

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
}