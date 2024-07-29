package com.navigaglobal.mobile.auth

import java.io.IOException

class ClientCredentialsAuthorizationProvider(private val clientId: String, private val clientSecret: String, private val tokenService: TokenService) :
    AuthorizationProvider {

    private var accessToken: String? = null
    private var expires: Long? = null

    override fun getAuthorization(): String? {
        accessToken.let { token ->
            if (expires ?: 0 > System.currentTimeMillis()) {
                return "Bearer $token"
            }
        }
        try {
            val tokenRequest = TokenRequest(clientId, clientSecret)
            val response = tokenService.token(tokenRequest).execute()
            if (response.isSuccessful) {
                response.body()?.let {
                    expires = System.currentTimeMillis() + ( it.expiresIn * 1000) - MARGIN
                    accessToken = it.accessToken
                    return "Bearer ${it.accessToken}"
                }
            }
            else {
                response.errorBody()?.string()?.let {
                    throw IOException("Failed to fetch token: $it")
                }
            }
        }
        catch (e: IOException) {
            throw IOException("Failed to fetch token", e)
        }
        return null
    }

    companion object {
        private const val MARGIN: Int = 5000
    }
}