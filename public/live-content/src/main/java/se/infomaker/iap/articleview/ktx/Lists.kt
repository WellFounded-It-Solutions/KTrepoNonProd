package se.infomaker.iap.articleview.ktx

internal fun List<String>.suffixItems(suffix: String): List<String> = this.map { it + suffix }