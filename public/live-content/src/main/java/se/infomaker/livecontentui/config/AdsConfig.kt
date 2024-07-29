package se.infomaker.livecontentui.config

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.json.JSONException
import org.json.JSONObject
import se.infomaker.livecontentui.extensions.patch
import timber.log.Timber

data class AdsConfig(
    var provider: String? = null,
    var providerConfiguration: List<JsonObject>? = null,
    var title: String = "",
    var distanceMin: Int = 0,
    var distanceMax: Int = 0,
    private var startIndex: Int? = null,
    var sticky: StickyAdsConfig? = null,
    var stickyArticle: StickyAdsConfig? = null
) {

    fun getStartIndex(): Int {
        return startIndex ?: distanceMin
    }
}

data class StickyAdsConfig(
    @SerializedName("providerConfiguration") private val _providerConfiguration: JsonObject?,
    @SerializedName("position") private val _position: String?
) {
    val rawProviderConfiguration: String
        get() = _providerConfiguration.toString()

    val position: String
        get() = _position ?: "bottom"

    companion object {
        @JvmStatic
        val DEFAULT_PROVIDER_CONFIG: JSONObject
            get() = JSONObject().apply {
                put("height", 50)
                put("width", 320)
            }
    }
}

val StickyAdsConfig?.providerConfiguration: JSONObject?
    get() = if (this?.rawProviderConfiguration != null) {
        try {
            StickyAdsConfig.DEFAULT_PROVIDER_CONFIG.patch(JSONObject(rawProviderConfiguration))
        }
        catch (e: JSONException) {
            Timber.e(e, "Failed to resolve sticky ad configuration.")
            null
        }
    }
    else {
        Timber.e("No sticky ad configuration to resolve.")
        null
    }