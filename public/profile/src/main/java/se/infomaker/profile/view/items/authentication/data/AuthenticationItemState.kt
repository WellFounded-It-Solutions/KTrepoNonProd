package se.infomaker.profile.view.items.authentication.data

import se.infomaker.iap.provisioning.LoginStatus

data class AuthenticationItemState (
    val loginState: LoginStatus = LoginStatus.IN_PROGRESS,
    val userName: String? = null,

)