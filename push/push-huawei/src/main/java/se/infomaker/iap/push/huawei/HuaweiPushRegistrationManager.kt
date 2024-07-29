package se.infomaker.iap.push.huawei

import android.content.Context
import androidx.annotation.WorkerThread
import com.huawei.hms.push.HmsMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import se.infomaker.frt.remotenotification.PushMeta
import se.infomaker.frt.remotenotification.PushRegistrationManager
import se.infomaker.iap.push.huawei.di.HuaweiAppId
import se.infomaker.iap.push.huawei.token.TokenManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HuaweiPushRegistrationManager(
    private val context: Context,
    private val appId: String,
    private val huaweiPushConfig: HuaweiPushConfig,
    private val tokenManager: TokenManager,
    private val scope: CoroutineScope
) : PushRegistrationManager {

    @Inject constructor(
        @ApplicationContext context: Context,
        @HuaweiAppId appId: String,
        huaweiPushConfig: HuaweiPushConfig,
        tokenManager: TokenManager
    ) : this(context, appId, huaweiPushConfig, tokenManager, CoroutineScope(Dispatchers.IO + Job()))

    override val pushMeta: PushMeta?
        get() {
            return if (tokenManager.token != null) {
                PushMeta(
                    "huawei",
                    appId = appId,
                    token = tokenManager.token
                )
            }
            else null
        }

    override fun registrationChanges(): Flow<Unit> {
        return tokenManager.tokenChanges
            .distinctUntilChanged()
            .drop(1)
            .mapToUnit()
    }

    override fun ensureRegistered() {
        scope.launch {
            tokenManager.retrieveToken(appId)
            subscribe()
        }
    }

    @WorkerThread
    private fun subscribe() {
        try {
            HmsMessaging.getInstance(context).subscribe(huaweiPushConfig.topicName)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { e ->
                            Timber.e(e, "Subscription to ${huaweiPushConfig.topicName} completed but failed.")
                        } ?: run {
                            Timber.e("Subscription to ${huaweiPushConfig.topicName} completed but failed, no exception.")
                        }
                    }
                }
        }
        catch (e: Exception) {
            Timber.e(e, "Subscription to ${huaweiPushConfig.topicName} failed.")
        }
    }
}

private fun <T> Flow<T>.mapToUnit() = map {  }
