package se.infomaker.iap.provisioning.firebase.auth

import com.google.gson.annotations.SerializedName

data class AccessToken(@SerializedName("access_token") val accessToken: String,
                       @SerializedName("expires_in") val expiresIn: Int,
                       @SerializedName("token_type") val tokenType: String) {
    private val expires = System.currentTimeMillis() + ( expiresIn * 1000) - EXPIRE_MARGIN

    fun authHeader(): String {
        return "$tokenType $accessToken"
    }

    fun isValid(): Boolean {
        return expires > System.currentTimeMillis()
    }

    companion object {
        private const val EXPIRE_MARGIN: Int = 5000
    }
}
