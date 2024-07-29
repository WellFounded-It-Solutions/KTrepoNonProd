package se.infomaker.livecontentmanager.config

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap

open class LiveContentConfig(
    @SerializedName("stream") private val _stream: StreamConfig? = null,
    @SerializedName("search") private val _search: SearchConfig? = null,
    @SerializedName("locationSearch") private val _locationSearch: LocationSearchConfig? = null,
    @SerializedName("timeZone") private val _timeZone: String? = null,
    @SerializedName("dateFormat") private val _dateFormat: String? = null,
    @SerializedName("geoPointsKey") private val _geoPointsKey: String? = null,
    @SerializedName("defaultPropertyMap") private val _defaultPropertyMap: String? = null
) : Serializable {
    var lcc: LiveContentCloudConfig? = null
    var streamRefreshThreshold: Int = 2
    var eventNotifierBroadcastId: String? = null
    var pagingLimit: Int = 20
    var liveContentUrl: String? = null
    val geoPointsKey: String
        get() = _geoPointsKey ?: ""
    val dateFormat: String
        get() = _dateFormat ?: "yyyy-MM-dd'T'HH:mm:ss'Z'"
    val timeZone: String
        get() = _timeZone ?: "UTC"
    val locationSearch: LocationSearchConfig
        get() = _locationSearch ?: LocationSearchConfig()
    val search: SearchConfig
        get() = _search ?: SearchConfig()
    val stream: StreamConfig
        get() = _stream ?: StreamConfig()
    val defaultPropertyMap: String
        get() = _defaultPropertyMap ?: "Article"
    var typePropertyMap: HashMap<String, Map<String, PropertyConfig>>? = null
    var typeDescriptionTemplate: Map<String, String>? = null
    var transformSettings: TransformSettingsConfig = TransformSettingsConfig()
    var conceptField: String? = null
    var storyField: String? = null
    var authorField: String? = null

    @SerializedName("subListBehaviour")
    private var _subListBehaviour: PresentationBehaviour? = null
    val subListBehaviour: PresentationBehaviour
        get() = _subListBehaviour ?: PresentationBehaviour.DEFAULT

    var conceptTypeUuidsMap: Map<String, String>? = null

    // LCC
    var infocasterToken: String? = null
    var infocaster: String? = null
        get() {
            infocasterToken?.let { token ->
                field?.run {
                    return field + "?authorization=${token}"
                }
            }
            return field
        }
    var querystreamer: String? = null
    var querystreamerId: String? = null
        get() = field ?: id ?: querystreamerUsername
    var querystreamerReadToken: String? = null
        get() = field ?: readToken ?: querystreamerPassword
    var id: String? = null
        get() = field ?: querystreamerUsername
    var readToken: String? = null
        get() = field ?: querystreamerPassword
    var querystreamerUsername: String? = null
    var querystreamerPassword: String? = null
    var querystreamerV2: String? = null
    var querystreamerClientId: String? = null
        get() = field ?: clientId
    var querystreamerClientSecret: String? = null
        get() = field ?: clientSecret

    var opencontent: String? = null
    var opencontentUsername: String? = null
    var opencontentPassword: String? = null
    var opencontentClientId: String? = null
        get() = field ?: clientId
    var opencontentClientSecret: String? = null
        get() = field ?: clientSecret

    var clientId: String? = null
    var clientSecret: String? = null
    var tokenServiceUrl: String? = null

    val defaultProperties by lazy { getProperties(defaultPropertyMap) }

    fun getProperties(entity: String): String {
        typePropertyMap?.get(entity)?.let {
            val builder = StringBuilder()
            createModel(it, builder)
            return builder.toString()
        }
        return ""
    }

    private fun createModel(model: Map<String, PropertyConfig>, builder: StringBuilder) {

        val values = model.values.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                .distinctBy { "${it.name}${it.propertyMapReference}" }

        values.forEachIndexed{ index, value ->
            builder.append(value.name)
            value.propertyMapReference?.let {
                builder.append("[")
                typePropertyMap?.get(it)?.let { subModel ->
                    createModel(subModel, builder)
                }
                builder.append("]")
            }
            if (values.size - 1 > index ) {
                builder.append(",")
            }
        }
    }

    fun getListProperties(entity: String): List<String>{
        typePropertyMap?.get(entity)?.let {
            return listProperties(it)
        }
        return emptyList()
    }

    private fun listProperties(map : Map<String, PropertyConfig>, prefix:String = "") : List<String> {
        val listProperties = ArrayList<String>()
        map.forEach { (_, value) ->
            if (value.propertyMapReference == null) {
                listProperties.add("$prefix${value.name}")
            } else {
                value.propertyMapReference?.let { type ->
                    typePropertyMap?.get(type)?.let { innerMap ->
                        val prefix = value.name.let { "$prefix${value.name}." }
                        listProperties.addAll(listProperties(innerMap, prefix))
                    }
                }
            }
        }
        listProperties.sort()
        return listProperties
    }
}

internal data class LiveContentConfigWrapper(@SerializedName("liveContent") val _liveContent: LiveContentConfig? = null) {
    val liveContent: LiveContentConfig
        get() = _liveContent ?: LiveContentConfig()
}