package se.infomaker.frt.remotenotification

data class PushMeta(
    val type: String,
    val arn: String? = null,
    val platform: String? = null,
    val ttl: Int? = null,
    val appId: String? = null,
    val token: String? = null
)
