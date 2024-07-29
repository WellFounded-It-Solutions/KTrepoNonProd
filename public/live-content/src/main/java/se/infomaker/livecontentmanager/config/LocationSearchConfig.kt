package se.infomaker.livecontentmanager.config

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class LocationSearchConfig(
    @SerializedName("geometryKey") private val _geometryKey: String? = null,
    @SerializedName("locationNameProperty") private val _locationNameProperty: String? = null
) : SearchConfig(), Serializable {
    val geometryKey: String
        get() = _geometryKey ?: ""
    val locationNameProperty: String
        get() = _locationNameProperty ?: ""
}