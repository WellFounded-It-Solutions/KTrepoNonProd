package se.infomaker.livecontentui.section.ads

import android.content.Context
import org.json.JSONObject
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import timber.log.Timber

/**
 * Provides state usable as ad context
 */
class AdStateManager private constructor(context: Context) {
    private var current = JSONObject()

    init {
        val loginManager = ProvisioningManagerProvider.provide(context).loginManager()
        setLoggedIn(loginManager?.isUserLoggedIn() == true)
        val disposable = loginManager?.loginStatus()?.subscribe({ setLoggedIn(it == LoginStatus.LOGGED_IN) }, {
            Timber.e(it, "Failed to update ad context")
        })
    }

    private fun setLoggedIn(isLoggedIn: Boolean) {
        current.put("isLoggedin", isLoggedIn)
        current.put("isLoggedinInt", if (isLoggedIn) "1" else "0")
    }

    companion object {
        private var INSTANCE: AdStateManager? = null

        @JvmStatic
        fun get(context: Context): JSONObject {
            INSTANCE?.let {
                return it.current
            }
            AdStateManager(context).let {
                INSTANCE = it
                return it.current
            }
        }
    }
}