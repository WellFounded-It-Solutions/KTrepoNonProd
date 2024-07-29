package se.infomaker.iap.push.huawei.token

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.datastore.preferences.core.edit
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager(
    private val context: Context,
    private val scope: CoroutineScope
) {

    @Inject constructor(@ApplicationContext context: Context) : this(context, CoroutineScope(Dispatchers.IO + Job()))

    private var _token: String? = null
    val token: String?
        get() = _token

    val tokenChanges = context.tokenStore.data
        .map { it[PreferencesKeys.TOKEN] }

    init {
        scope.launch {
            context.tokenStore.data
                .map { it[PreferencesKeys.TOKEN] }
                .collect { _token = it }
        }
    }

    @WorkerThread
    internal fun retrieveToken(appId: String) {
        try {
            // Set tokenScope to HCM.
            val tokenScope = "HCM"
            val token = HmsInstanceId.getInstance(context).getToken(appId, tokenScope)

            // Check whether the token is empty.
            if (!token.isNullOrEmpty()) {
                onToken(token)
            }
        } catch (e: ApiException) {
            Timber.e(e, "Failed retrieving token.")
        }
    }

    internal fun onToken(token: String) {
        scope.launch {
            context.tokenStore.edit {
                val currentToken = it[PreferencesKeys.TOKEN]
                if (token != currentToken) {
                    it[PreferencesKeys.TOKEN] = token
                }
            }
        }
    }
}