package se.infomaker.iap.push.google

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import se.infomaker.frt.remotenotification.PushMeta
import se.infomaker.frt.remotenotification.PushRegistrationManager
import se.infomaker.iap.push.google.api.Registration
import se.infomaker.iap.push.google.api.RegistrationResult
import se.infomaker.iap.push.google.api.RegistrationService
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePushRegistrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pushConfig: FirebasePushConfig,
    private val pushRegistrationService: RegistrationService
) : PushRegistrationManager {

    private var deviceIdChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override val pushMeta: PushMeta?
        get() = FCMUtil.getDeviceId(context)?.let { deviceId ->
            PushMeta(
                "sns",
                arn = deviceId,
                platform = "GCM",
                ttl = DEFAULT_PUSH_TTL
            )
        }

    init {
        if (canUnregister()) {
            if (FCMUtil.getPushUnregisterUrl(context).isNullOrEmpty()) {
                Timber.d("No previously pushUnregisterURL stored using URL from config pushUnregisterURL=${pushConfig.pushUnregisterURL}")
                FCMUtil.addUnregisterUrl(context, pushConfig.pushUnregisterURL)
            }
        }
    }

    override fun registrationChanges(): Flow<Unit> = callbackFlow {
        deviceIdChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if ("deviceId" == key) {
                trySend(Unit)
            }
        }

        FCMUtil.registerForDeviceIdChange(context, deviceIdChangeListener)

        awaitClose { FCMUtil.unregisterForDeviceIdChange(context, deviceIdChangeListener) }
    }

    override fun ensureRegistered() {
        if (didPreviousRegistrationFail()) {
            FCMUtil.getToken(context)?.let { token ->
                sendRegistrationToServer(token)
            }
        }
        else if (canUnregister() && shouldUnregister()) {
            renewRegistration()
        }
        else if (noTokenAvailable()) {
            retrieveToken { token ->
                FCMUtil.setToken(context, token)
                FCMUtil.removeRegistration(context)
                sendRegistrationToServer(token)
            }
        }
    }

    private fun canUnregister() =
        if (!FCMUtil.getPushUnregisterUrl(context).isNullOrEmpty()) true else !pushConfig.pushUnregisterURL.isNullOrEmpty()

    private  fun didPreviousRegistrationFail(): Boolean {
        FCMUtil.getToken(context)?.let { token ->
            if (FCMUtil.getDeviceId(context).isNullOrEmpty() && token.isNotEmpty()) {
                Timber.d("Previous registration failed.  Try to re-register.")
                // We have no stored deviceId but we have a token. Which means
                // something went wrong with the previous registration attempt.
                return true
            }
        }
        return false
    }

    private fun noTokenAvailable(): Boolean {
        return FCMUtil.getToken(context) == null
    }

    private fun retrieveToken(onSuccess: ((token: String) -> Unit)) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null) {
                    onSuccess(result)
                }
                else {
                    Timber.w("FCM Token retrieval succeeded, but no token in result.")
                }
            } else {
                Timber.e(task.exception, "FCM Token retrieval failed.")
            }
        }
    }

    private fun renewRegistration() {
        FCMUtil.getToken(context)?.let { token ->
            Timber.d("Updating remote push configuration.")
            unregister {
                sendRegistrationToServer(token)
            }
        }
    }

    fun sendRegistrationToServer(token: String) {
        try {
            val registration = Registration(token, pushConfig.pushTopic, pushConfig.pushApplication)
            pushRegistrationService.register(pushConfig.pushRegisterURL, registration).enqueue(
                object : Callback<RegistrationResult> {
                    override fun onResponse(
                        call: Call<RegistrationResult>,
                        response: Response<RegistrationResult>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.run {
                                Timber.d(
                                    "Device registered for remote push using ARN=%s.",
                                    this.deviceId
                                )
                                FCMUtil.updateRegistration(context, deviceId, token)
                                FCMUtil.updateRegistrationConfig(
                                    context,
                                    pushConfig.pushTopic,
                                    pushConfig.pushApplication,
                                    pushConfig.pushRegisterURL,
                                    pushConfig.pushUnregisterURL
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<RegistrationResult>, t: Throwable) {
                        Timber.e(t, "Unable to remove previously stored registration.")
                    }
                }
            )
        } catch (e: IOException) {
            Timber.e(e, "Failed to send token to server.")
        }
    }

    private fun shouldUnregister(): Boolean {
        val deviceId = FCMUtil.getDeviceId(context)
        deviceId?.run { // We have a previously stored device id
            Timber.d("should unregister :: we have a device id")
            val savedPushConfig = getStoredConfig(context)
            // Unregister if the topic, app or registration url changes
            if (savedPushConfig.pushTopic != null && savedPushConfig.pushApplication != null && savedPushConfig.pushRegisterURL != null) {
                return savedPushConfig.pushTopic != pushConfig.pushTopic ||
                        savedPushConfig.pushApplication != pushConfig.pushApplication ||
                        savedPushConfig.pushRegisterURL != pushConfig.pushRegisterURL
            }
        }
        return false
    }

    private fun unregister(whenDone: (() -> Unit)) {
        try {
            val pushUnregisterURL = FCMUtil.getPushUnregisterUrl(context)
            val storedDeviceId = FCMUtil.getDeviceId(context)
            if (storedDeviceId.isNullOrEmpty()) { // We have no previously stored device id
                Timber.d("No previously deviceId stored. Unable to unregister.")
                whenDone.invoke()
                return
            }

            Timber.d("Unregistering app with ARN=$storedDeviceId")
            pushRegistrationService.unregister(pushUnregisterURL, Registration(storedDeviceId)).enqueue(
                object : Callback<RegistrationResult> {
                    override fun onResponse(
                        call: Call<RegistrationResult>,
                        response: Response<RegistrationResult>
                    ) {
                        if (response.isSuccessful || response.code() == 400) {
                            Timber.d("Previously stored registration removed.")
                            FCMUtil.removeRegistration(context)
                            whenDone.invoke()
                        }
                    }

                    override fun onFailure(call: Call<RegistrationResult>, t: Throwable) {
                        Timber.e(t, "Unable to remove previously stored registration.")
                    }
                })
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister device.")
        }
    }

    companion object {
        const val DEFAULT_PUSH_TTL = 86400 // 24hours in seconds

        fun getStoredConfig(context: Context): FirebasePushConfig {
            return FirebasePushConfig(
                FCMUtil.getApplication(context),
                FCMUtil.getPushTopic(context),
                FCMUtil.getPushRegisterUrl(context),
                FCMUtil.getPushUnregisterUrl(context)
            )
        }
    }
}