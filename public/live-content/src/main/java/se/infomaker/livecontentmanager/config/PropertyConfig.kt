package se.infomaker.livecontentmanager.config

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PropertyConfig(
    @SerializedName("name") private val _name: String? = null,
    var type: String? = null,
    var propertyMapReference: String? = null,
    var transforms: List<String>? = null,
    var operators: List<OperatorConfig>? = null
): Serializable {
    val name: String
        get() = _name ?: throw IllegalArgumentException("A property needs a name.")
}

data class OperatorConfig(
    @SerializedName("type") private val _type: String? = null,
    var params: Map<String, String>? = null
) {
    val type: String
        get() = _type ?: throw IllegalArgumentException("An operator needs a type.")
}
