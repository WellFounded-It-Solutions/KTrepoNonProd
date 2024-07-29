package se.infomaker.profile.view.items.authentication.data

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.navigaglobal.mobile.profile.R
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import timber.log.Timber


data class AuthenticationDialogState(
    val visible: Boolean,
    val logoutDialogStrings: AuthenticationItemViewModel.LogoutDialogStrings?,
    val dismissAction: (() -> Unit)? = null,
    val authAction: (() -> Unit)? = null,
)

internal val Context.dataStore by preferencesDataStore("profile_preferences")

@FlowPreview
class AuthenticationItemViewModel(app: Application) : AndroidViewModel(app) {

    private val loginManager by lazy {
        ProvisioningManagerProvider.provide(getApplication()).loginManager()
    }
    private val usernameKey = stringPreferencesKey("USER_NAME")
    private val loginStatusKey = intPreferencesKey("LOGIN_STATUS")

    private val usernameFromDataStoreAsFlow =
        getApplication<Application>().applicationContext.dataStore.data
            .map { it[usernameKey] }

    private val usernameAsFlow =
        flowOf(userInfoAsFlow(), usernameFromDataStoreAsFlow)
            .flattenMerge()
            .distinctUntilChanged()

    private val loginStatusFromDataStoreAsFlow =
        getApplication<Application>().applicationContext.dataStore.data
            .map { it[loginStatusKey] }
            .filterNotNull()
            .map { LoginStatus.values()[it] }

    private val loginStatusFromLoginManagerStatusAsFlow = loginManager
        ?.loginStatus()
        ?.asFlow()
        ?.catch { Timber.d(it) } ?: emptyFlow()

    private val loginStatusAsFlow =
        flowOf(
            loginStatusFromLoginManagerStatusAsFlow,
            loginStatusFromDataStoreAsFlow
        )
            .flattenMerge()
            .distinctUntilChanged()
            .filterNotNull()

    private val _usernameState = MutableStateFlow("")
    val usernameState: StateFlow<String> = _usernameState

    private val _loginStatus = MutableStateFlow(LoginStatus.LOGGED_OUT)
    val loginStatusState: StateFlow<LoginStatus> = _loginStatus

    val authenticationDialogState: MutableState<AuthenticationDialogState> =
        mutableStateOf(
            AuthenticationDialogState(
                logoutDialogStrings = logoutDialogStrings,
                visible = false
            )
        )

    private val logoutDialogStrings: LogoutDialogStrings
        get() {
            val resources: Resources = (getApplication() as Context).resources
            return LogoutDialogStrings(
                message = resources.getString(R.string.my_profile_log_out_message),
                confirm = resources.getString(R.string.my_profile_log_out),
                cancel = resources.getString(R.string.my_profile_cancel)
            )
        }

    init {
        viewModelScope.launch {
            usernameAsFlow.collect { name ->
                name?.let {
                    _usernameState.value = name
                    writeToDataStore(usernameKey, name)
                }
            }
        }
        viewModelScope.launch {
            loginStatusAsFlow.collect { status ->
                _loginStatus.value = status
                writeToDataStore(loginStatusKey, status.ordinal)
                // Handle case of newly installed app.
                if (_usernameState.value.isEmpty() && status == LoginStatus.LOGGED_IN) {
                    userInfoAsFlow().collect { name ->
                        name?.let {
                            _usernameState.value = name
                            writeToDataStore(usernameKey, name)
                        }
                    }
                }
            }
        }
    }

    private fun <T> writeToDataStore(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            getApplication<Application>().applicationContext.dataStore.edit {
                it[key] = value
            }
        }
    }

    private fun userInfoAsFlow() = flow {
        try {
            val result =
                loginManager?.userInfo()?.toDeferredAsync(CoroutineScope(Dispatchers.IO))?.await()
            emit(result?.displayName)
        } catch (e: Exception) {
            Timber.d("${e.message}")
            emit(null)
        }
    }

    data class LogoutDialogStrings(
        val message: String? = null,
        val confirm: String? = null,
        val cancel: String? = null,
    )

    fun onTriggerEvent(event: AuthenticationItemEvent) {
        when (event) {
            is AuthenticationItemEvent.Login -> {
                login(context = event.context)
            }

            is AuthenticationItemEvent.Logout -> {
                logout(context = event.context)
            }
        }
    }

    private fun logout(context: Context) {
        loginManager?.logout(context.requireActivity()) {
            writeToDataStore(loginStatusKey, LoginStatus.LOGGED_OUT.ordinal)
            writeToDataStore(usernameKey, "")
        }
    }

    private fun login(context: Context) {
        loginManager?.showLogin(context.requireActivity())
    }

    private fun <T> Single<T>.toDeferredAsync(coroutineScope: CoroutineScope) =
        coroutineScope.async { await() }
}