package com.navigaglobal.mobile.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {

    @POST("/v1/token")
    fun token(@Body request: TokenRequest): Call<TokenResponse>
}