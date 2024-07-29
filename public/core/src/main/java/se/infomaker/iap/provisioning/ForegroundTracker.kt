package se.infomaker.iap.provisioning

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

@SuppressLint("StaticFieldLeak")
object ForegroundTracker : Application.ActivityLifecycleCallbacks {
    private var isAttached = false

    @Synchronized
    fun attach(context: Context) {
        if (!isAttached) {
            isAttached = true
            (context.applicationContext as Application).registerActivityLifecycleCallbacks(ForegroundTracker)
        }
    }

    private var relay = BehaviorRelay.createDefault(false)
    var foregroundActivity: Activity? = null
    var isInForeground: Boolean
        set(value) {
            relay.accept(value)
        }
        get() {
            return relay.value == true
        }

    @Deprecated(message = "Functionality has been rewritten on top of AndroidX Lifecycle in utilities.",
            replaceWith = ReplaceWith("ForegroundDetector.observable()", "se.infomaker.frtutilities.ForegroundDetector"))
    fun observe(): Observable<Boolean> {
        return relay
    }

    override fun onActivityPaused(p0: Activity) {
        isInForeground = false
        if (foregroundActivity == p0) {
            foregroundActivity = null
        }
    }

    override fun onActivityResumed(p0: Activity) {
        isInForeground = true
        foregroundActivity = p0
    }

    override fun onActivityStarted(p0: Activity) {}

    override fun onActivityDestroyed(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

}