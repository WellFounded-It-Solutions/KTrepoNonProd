package se.infomaker.livecontentmanager.parser

import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.frtutilities.DateUtil
import se.infomaker.livecontentmanager.extensions.similar
import java.io.Serializable
import java.util.Date
import java.util.UUID

open class PropertyObject(var properties: JSONObject, val id: String) : Serializable {

    private val LOCK = Any()
    private val stringOverrides = mutableMapOf<String, String>()

    private var parsedPublicationDate: Date? = null
    private var legacyMap: Map<String, List<String>>? = null
    private var legacyFlatMap: Map<String, String>? = null
    private var description: Map<String, String>? = null

    val contentType: String?
        get() = optStringOrNull("contentType")

    val name: String?
        get() = optStringOrNull("name")

    val conceptType: String?
        get() = optStringOrNull("conceptType")

    val externalLink: String?
        get() = optStringOrNull("externalLinkUrl")

    private fun writeObject(out: java.io.ObjectOutputStream) {
        out.writeUTF(properties.toString())
    }


    private fun readObject(input: java.io.ObjectInputStream) {
        properties = JSONObject(input.readUTF())
    }

    open fun getPublicationDate(): Date {
        if (parsedPublicationDate == null) {
            synchronized(LOCK) {
                if (parsedPublicationDate == null) {
                    parsedPublicationDate = parsePublicationDate()
                }
            }
        }
        return parsedPublicationDate!!
    }

    private fun parsePublicationDate(): Date? {
        val dateList = properties.optJSONArray("publicationDate")
        if (dateList != null && dateList.length() > 0) {
            return DateUtil.getDateFromString(dateList.getString(0))
        }
        return Date(0)
    }

    fun legacyFormatFlat(): Map<String, String> {
        legacyFlatMap?.let {
            return it
        }
        synchronized(LOCK) {
            if (legacyFlatMap == null) {
                val map = mutableMapOf<String, String>()
                properties.keys().forEach { key ->
                    optString(key)?.let {
                        map[key] = it
                    }
                }
                stringOverrides.keys.forEach{ key ->
                    stringOverrides[key]?.let {
                        map[key] = it
                    }
                }
                legacyFlatMap = map
            }
            return legacyFlatMap!!
        }
    }

    fun legacyFormat(): Map<String, List<String>> {
        if (legacyMap == null) {
            synchronized(LOCK) {
                if (legacyMap == null) {
                    val map = mutableMapOf<String, List<String>>()
                    properties.keys().forEach { key ->
                        optStringList(key)?.let {
                            map[key] =  it
                        }
                    }
                    legacyMap = map
                }
            }
        }

        return legacyMap!!
    }

    fun optPropertyObjects(vararg keyPath: String): List<PropertyObject>? {
        var containing : MutableList<PropertyObject>? = null
        keyPath.forEach { path ->
            val jsonArray = properties.optJSONArray(path)
            if (jsonArray != null) {
                val list = mutableListOf<PropertyObject>()
                for (i in 0..jsonArray.length()) {
                    jsonArray.optJSONObject(i)?.let {
                        val uuid = it.optStringFromKeyPath("uuid", it.optStringFromKeyPath("contentId", null)) ?: "RANDOM-" + UUID.randomUUID().toString()
                        list.add(PropertyObject(it, uuid))
                    }
                }
                if (containing == null && list.isNotEmpty()) {
                    containing = mutableListOf()
                }
                containing?.addAll(list)
            }
        }
        return containing
    }

    fun optPropertyObject(keyPath: String) : PropertyObject? {
        return optPropertyObjects(keyPath)?.get(0)
    }

    fun optString(keyPath: String): String? {
        return  optString(keyPath, null)
    }

    fun putString(kePath: String, value: String) {
        stringOverrides.put(kePath, value)
        legacyFlatMap = null
    }

    fun optString(keyPath: String, fallback: String?): String? {
        return stringOverrides[keyPath] ?: properties.optStringFromKeyPath(keyPath, fallback)
    }

    fun optStringOrNull(keyPath: String): String? {
        return optString(keyPath, null)
    }

    fun optStringList(keyPath: String): List<String>? {
        properties.optArrayFromKeyPath(keyPath)?.let { array ->
            return (0..array.length() - 1).map {
                array.getString(it)
            }
        }
        return null
    }

    fun optJSONArray(keyPath: String): JSONArray? {
        return properties.optArrayFromKeyPath(keyPath)
    }

    /**
     * Digs out first string value of a property keypath
     */
    private fun JSONObject.optStringFromKeyPath(keyPath: String, fallback: String?): String? {
        optArrayFromKeyPath(keyPath)?.let {
            if (it.length() > 0) {
                return it.optString(0)
            }
            return fallback
        }
        return fallback
    }

    private fun JSONObject.optArrayFromKeyPath(keyPath: String): JSONArray? {
        val parts = keyPath.split(".")
        var node = this
        (0..parts.size - 2)
                .asSequence()
                .map { node.optJSONArray(parts[it]) }
                .forEach {
                    if (it != null) {
                        if (it.length() > 0) {
                            node = it.getJSONObject(0)
                        } else {
                            return null
                        }
                    } else {
                        return null
                    }
                }
        return node.optJSONArray(parts[parts.size - 1])
    }

    fun areContentsTheSame(propertyObject : PropertyObject) : Boolean {
        return properties.similar(propertyObject.properties) && stringOverrides == propertyObject.stringOverrides
    }

    /**
     * Returns a flat description of the object, useful for statistics events
     */
    fun describe(): Map<String, String> {
        if (description == null) {
            val out = mutableMapOf<String, String>()
            properties.optJSONObject(INTERNAL_DESCRIPTION)?.let {
                it.keys().forEach { key ->
                    if (!forbiddenKeys.contains(key)) {
                        out[key] = it.optString(key)
                    }
                }
            }
            description = out.toMap()
        }

        return description!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        (other as? PropertyObject)?.let {
            return areContentsTheSame(it)
        }

        return false
    }

    override fun hashCode(): Int {
        var result = properties.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "PropertyObject(properties=$properties, id='$id')"
    }

    companion object {
        const val NO_UUID = "00000000-0000-0000-0000-000000000000"
        const val INTERNAL_DESCRIPTION = "INTERNAL-Description"

        private val forbiddenKeys = listOf("newsML", "authorsRaw")
    }
}

