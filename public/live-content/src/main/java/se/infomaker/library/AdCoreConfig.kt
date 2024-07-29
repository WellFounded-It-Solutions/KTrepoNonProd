package se.infomaker.library

import com.google.gson.JsonObject

data class AdCoreConfig(val adProviders: List<AdProviderCoreConfig>?)

data class AdProviderCoreConfig(val provider: String, val config: JsonObject?)
