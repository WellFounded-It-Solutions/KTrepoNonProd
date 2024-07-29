package com.navigaglobal.mobile.auth

import okhttp3.Credentials

class BasicAuthAuthorizationProvider(username: String, password: String) : AuthorizationProvider {
    private val basic = Credentials.basic(username, password)
    override fun getAuthorization(): String? = basic
}