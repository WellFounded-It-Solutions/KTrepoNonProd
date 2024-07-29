package se.infomaker.iap.push.google

data class FirebasePushConfig(
    val pushApplication: String?,
    val pushTopic: String?,
    val pushRegisterURL: String?,
    val pushUnregisterURL: String?
)