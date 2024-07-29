package se.infomaker.livecontentui.config

data class SharingConfig(
    val shareApiUrl: String? = null,
    val shareKey: String? = null,
    val preferredShareSource: String? = null,
    val titleKey: String? = null,
    val allowedDomains: List<String>? = null
)