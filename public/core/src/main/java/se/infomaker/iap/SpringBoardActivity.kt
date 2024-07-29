package se.infomaker.iap

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.navigaglobal.mobile.R
import dagger.hilt.android.AndroidEntryPoint
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.iap.update.Update
import se.infomaker.iap.update.UpdateManager
import se.infomaker.iap.update.UpdateType
import se.infomaker.iap.update.ui.UpdateActivity
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SpringBoardActivity : AppCompatActivity() {

    @Inject lateinit var updateManager: UpdateManager

    private var shouldReportViewShow = true

    override fun onCreate(savedInstanceState: Bundle?) {

        Timber.d("SplashScreenActivity")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
        }

        super.onCreate(savedInstanceState)

        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(CancelDrawListener)

        if (savedInstanceState != null) {
            val destroyTime = savedInstanceState.getLong(DESTROY_TIME_KEY, 0)
            shouldReportViewShow = System.currentTimeMillis() - destroyTime > RECREATION_TIMEOUT
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(DESTROY_TIME_KEY, System.currentTimeMillis())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.removeOnPreDrawListener(CancelDrawListener)
    }

    override fun onResume() {
        super.onResume()

        if (shouldReportViewShow) {
            StatisticsEvent.Builder()
                .viewShow()
                .viewName("splash")
                .build()
                .also { event ->
                    StatisticsManager.getInstance().logEvent(event)
                }
        }

        val initialUpdate = updateManager.get()
        when (initialUpdate.type) {
            UpdateType.NONE -> SpringBoardManager.route(this, intent, onComplete = {
                // Handle case where we got a new Update during routing.
                val latestUpdate = updateManager.get()
                if (latestUpdate.type == UpdateType.RECOMMENDED) {
                    openUpdateActivity(latestUpdate)
                }
                else {
                    finish()
                }
            })
            UpdateType.RECOMMENDED -> SpringBoardManager.route(this, intent, onComplete = { openUpdateActivity(initialUpdate) })
            else -> {
                SpringBoardManager.currentRoute()?.cancel()
                openUpdateActivity(initialUpdate)
            }
        }
    }

    private fun openUpdateActivity(update: Update) = UpdateActivity.openIfAllowed(this, update, intent) { finish() }

    companion object {
        private const val DESTROY_TIME_KEY = "destroyTime"
        private const val RECREATION_TIMEOUT = 700
    }
}

private object CancelDrawListener : ViewTreeObserver.OnPreDrawListener {
    override fun onPreDraw() = false
}