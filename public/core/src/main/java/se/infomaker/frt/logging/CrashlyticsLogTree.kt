package se.infomaker.frt.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsLogTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR || priority == Log.WARN) {
            val formattedMessage = LogMessageHelper.format(priority, tag, message)
            FirebaseCrashlytics.getInstance().recordException(StackTraceRecorder(formattedMessage))
        }
    }
}
