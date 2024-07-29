package se.infomaker.iap.provisioning.backend

import com.google.gson.annotations.SerializedName

data class RefreshAccesstokenRequest(@SerializedName("refresh_token") val refreshToken: String)
