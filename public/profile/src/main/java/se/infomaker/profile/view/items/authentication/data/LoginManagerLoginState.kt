package se.infomaker.profile.view.items.authentication.data

import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.backend.LoginType

data class LoginManagerLoginState(
    val status: LoginStatus,
    private val displayName: String? = null,
    val type: LoginType
) : LoginState {

    override fun getDisplayName(): String? = displayName

    override fun isLoggedIn(): Boolean = status == LoginStatus.LOGGED_IN

    override fun isLoading(): Boolean = status == LoginStatus.IN_PROGRESS

    override fun isLoginEnabled(): Boolean = LoginType.TEMPORARILY_DISABLED != type
}