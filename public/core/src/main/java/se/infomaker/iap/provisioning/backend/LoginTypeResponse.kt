package se.infomaker.iap.provisioning.backend

import com.google.gson.annotations.SerializedName

enum class LoginType(val value: String) {
    @SerializedName("url")
    URL("url"),

    @SerializedName("password")
    PASSWORD("password"),

    @SerializedName("temporarilyDisabled")
    TEMPORARILY_DISABLED("temporarilyDisabled"),

    @SerializedName("none")
    NONE("none"),

    UNAVAILABLE("unavailable")
}

data class LoginTypeResponse(val loginType: LoginType)