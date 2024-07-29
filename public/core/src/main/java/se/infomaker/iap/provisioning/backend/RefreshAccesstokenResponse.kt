package se.infomaker.iap.provisioning.backend

import com.google.gson.annotations.SerializedName
import java.util.*

data class RefreshAccesstokenResponse(
        @SerializedName("access_token")val accessToken: String,
        @SerializedName("expires_in")val expiresIn: Int,
        @SerializedName("scope")val scope: String
){
    val expireDate: Date = Date().also {
        it.time = it.time + (expiresIn * 1000)
    }
}
