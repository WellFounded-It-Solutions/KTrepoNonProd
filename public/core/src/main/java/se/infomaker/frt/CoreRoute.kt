package se.infomaker.frt

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.navigaglobal.mobile.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.infomaker.frt.ui.activity.MainActivity
import se.infomaker.iap.Cancellable
import se.infomaker.iap.Route
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.ProvisioningManager
import se.infomaker.iap.provisioning.backend.LoginType
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CoreRoute(private val provisioningManager: ProvisioningManager, private val onComplete: Function0<Unit>, private val onCancel: Function0<Unit>) : Route() {

    private var paywallPermissionJob: Job? = null

    fun handleAppStartPaywall(activity: Activity, intent: Intent) {

        paywallPermissionJob = GlobalScope.launch(Dispatchers.IO) {
            try {
                val canPass = checkPermissionToPassPaywall()

                if (!isActive) {
                    return@launch
                }

                val loginManager = provisioningManager.loginManager()
                when {
                    !canPass -> presentAppStartPaywall(activity)
                    loginManager == null -> startApp(activity, intent)
                    loginManager.getLoginStatus() != LoginStatus.LOGGED_IN && !loginManager.isLinked && !loginManager.userHasOptedOut -> {
                        val loginType = loginManager.loginType().singleOrError()?.onErrorReturnItem(LoginType.UNAVAILABLE)?.blockingGet()
                        if (loginType == LoginType.TEMPORARILY_DISABLED || loginType == LoginType.UNAVAILABLE) {
                            startApp(activity, intent)
                        }
                        else {
                            presentAppStartPaywall(activity)
                        }
                    }
                    else -> startApp(activity, intent)
                }
            }
            catch (t: Throwable) {
                Timber.e(t, "Could not determine if the paywall could be passed")
                try {
                    AlertDialog.Builder(activity).setMessage(R.string.could_not_verify_paywall)
                            .setPositiveButton(R.string.retry) { _, _ -> handleAppStartPaywall(activity, intent) }
                            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                            .setOnCancelListener { activity.finish() }
                            .show()
                } catch (e: Throwable) {
                    Timber.w(e, "Could not present UI")
                }
            }
        }
    }

    private suspend fun checkPermissionToPassPaywall(): Boolean = suspendCoroutine { continuation ->
        provisioningManager.checkPermissionToPassPaywall(onResult = {
            continuation.resume(it)
        }, onError = {
            continuation.resumeWithException(it)
        })
    }

    private fun presentAppStartPaywall(activity: Activity) {
        Timber.d("Presenting paywall")
        provisioningManager.presentAppStartPaywall(activity, onComplete)
    }

    fun startApp(activity: Activity, intent: Intent) {
        Timber.d("Starting app")
        val mainActivityIntent = Intent(activity, MainActivity::class.java)
        intent.extras?.let {
            mainActivityIntent.putExtras(it)
        }
        activity.startActivity(mainActivityIntent)
        onComplete.invoke()
    }

    override fun cancel() {
        paywallPermissionJob?.cancel()
        (provisioningManager as? Cancellable)?.cancel()
        onCancel.invoke()
    }
}