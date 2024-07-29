package se.infomaker.iap.provisioning.firebase

import com.google.gson.annotations.SerializedName
import se.infomaker.iap.provisioning.backend.LoginType

data class FirebaseProvisioningConfig(
    val redirectUri: String?,
    val appId: String,
    @Deprecated("Don't force LoginType, we will not be able to temporarily disable paywall.") val forceLoginType: LoginType?,
    val appStartPaywallPermission: String?,
    val enablePurchases: Boolean?,
    val enableLogin: Boolean?,
    val provideAccessTokens: Boolean?,
    val enableUserIdTracking: Boolean?,
    @SerializedName("useLocalEmulators") private val _useLocalEmulators: Boolean?,
    @SerializedName("region") private val _region: String? = null
) {

    val useLocalEmulators: Boolean
        get() = _useLocalEmulators ?: false

    val region: String
        get() = _region ?: "europe-west1"
}
