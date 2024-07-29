package se.infomaker.profile.view.items.authentication.data

import android.content.Context

sealed class AuthenticationItemEvent {
    data class Login(val context: Context) : AuthenticationItemEvent()
    data class Logout(val context: Context) : AuthenticationItemEvent()
}
