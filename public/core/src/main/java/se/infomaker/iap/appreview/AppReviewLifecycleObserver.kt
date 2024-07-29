package se.infomaker.iap.appreview

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import se.infomaker.iap.appreview.repository.AppReviewRepository

class AppReviewLifecycleObserver(
    context: Context,
    private val appReviewConfig: AppReviewConfig? = null
) : Application.ActivityLifecycleCallbacks {

    private var appReviewManager: AppReviewManager? = null

    init {
        AppReviewRepositoryProvider.configure(AppReviewRepository(context, appReviewConfig))
    }

    override fun onActivityResumed(activity: Activity) {
        val fragmentActivity = activity as? FragmentActivity
        fragmentActivity?.let {
            appReviewManager =
                AppReviewManager(appReviewConfig, AppReviewRepositoryProvider.provide())
            appReviewManager?.onCreate(it)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        AppReviewRepositoryProvider.provide().updateTotalUsageTime()
        appReviewManager?.onDestroy()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }
}