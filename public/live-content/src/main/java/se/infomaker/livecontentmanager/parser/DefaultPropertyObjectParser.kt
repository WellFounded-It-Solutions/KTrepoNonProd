package se.infomaker.livecontentmanager.parser

import android.text.Html
import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.frtutilities.DateUtil
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.livecontentmanager.config.PropertyConfig
import se.infomaker.livecontentmanager.config.TransformSettingsConfig
import se.infomaker.livecontentmanager.model.StreamEventWrapper
import se.infomaker.livecontentmanager.stream.EventType
import java.text.SimpleDateFormat
import java.util.Locale

class DefaultPropertyObjectParser(val typePropertyMap: Map<String, Map<String, PropertyConfig>>, val typeDescriptionTemplate: Map<String,String>, var transformSettings: TransformSettingsConfig) : PropertyObjectParser {
    val transformers: MutableMap<String, Transformer> = mutableMapOf()
    val operators: MutableMap<String, Operator> = mutableMapOf()

    init {
        transformers["uppercase"] = object : Transformer {
            override fun transform(input: String): String = input.toUpperCase()
        }
        transformers["lowercase"] = object : Transformer {
            override fun transform(input: String): String = input.toLowerCase()
        }
        transformers["capitalize"] = object : Transformer {
            override fun transform(input: String): String = input.capitalize()
        }
        transformers["htmlStrip"] = object : Transformer {
            override fun transform(input: String): String {
                return Html.fromHtml(input).toString()
            }
        }
        transformers["formatReplace"] = object : Transformer {
            override fun transform(input: String): String {
                var output = input
                transformSettings.formatReplace.forEach { (from, to) -> output = output.replace(from, to) }
                return output
            }
        }
        operators["preDateFormatter"] = object : Operator {
            override fun modify(input: String, parameters: Map<String, String>?): String {
                if (parameters?.get("dateFormat") != null) {
                    val timestamp = DateUtil.getDateFromString(input) ?: return input
                    val locale: String = parameters.get("locale") ?: "sv_SE"
                    val dateFormat = SimpleDateFormat(parameters["dateFormat"], Locale(locale))
                    return dateFormat.format(timestamp)
                }
                return input
            }
        }
    }

    override fun fromStreamNotification(event: JSONObject?, type: String?): StreamEventWrapper {
        if (event == null) {
            return StreamEventWrapper(EventType.UNKNOWN, emptyList())
        }
        val result = JSONObject(JSONUtil.getString(event, "payload.data.result"))
        val type = result.getString("eventtype")
        val list = listOf(objectFromNotification(result, type))
        return StreamEventWrapper(EventType.valueOf(type), list)
    }

    override fun fromSearch(response: JSONObject, type: String?): List<PropertyObject> {
        val hits = JSONUtil.optJSONArray(response, "payload.data.result.hits.hits") ?: return emptyList()
        if (hits != null && hits.length() > 0) {
            return (0 until hits.length()).map {
                hits.getJSONObject(it)
            }.map {
                toPropertyObject(it, type)
            }

        }
        return emptyList()
    }

    fun toPropertyObject(jsonObject: JSONObject, type: String?): PropertyObject {
        val properties = jsonObject.optJSONArray("versions")?.optJSONObject(0)?.optJSONObject("properties")
                ?: JSONObject()
        return PropertyObject(remapAndTransform(properties, propertyMapFor(type)), jsonObject.getString("id"))
    }

    override fun getAllIds(from: List<PropertyObject>, type: String): Set<String> {
        val ids = mutableSetOf<String>()
        from.forEach { propertyObject ->
            ids.add(propertyObject.id)
            typePropertyMap[type]?.filter { it.value.propertyMapReference != null }?.forEach {
                val list = propertyObject.optPropertyObjects(it.key)
                val subType = it.value.propertyMapReference
                if (list != null && list.isNotEmpty() && subType != null) {
                    ids.addAll(getAllIds(list, subType))
                }
            }
        }
        return ids
    }

    private fun propertyMapFor(type: String?): Map<String, PropertyConfig> {
        return typePropertyMap[type] ?: emptyMap()
    }

    fun objectFromNotification(jsonResult: JSONObject, type: String?): PropertyObject {
        val normalizedProperties = JSONObject()
        val streamProperties = jsonResult.getJSONArray("properties")
        (0 until (streamProperties.length())).map { streamProperties.optJSONObject(it) }
                .forEach { property ->
                    property.optJSONArray("values")?.let {
                        normalizedProperties.put(property.getString("name"), it)
                    }
                }
        val propertyObject = PropertyObject(remapAndTransform(normalizedProperties, propertyMapFor(type)), jsonResult.getString("uuid"))

        return propertyObject
    }

    private fun remapAndTransform(properties: JSONObject, propertyMap: Map<String, PropertyConfig>): JSONObject {
        val out = JSONObject()
        propertyMap.forEach { entry ->
            val targetKey = entry.key
            val mappingConfig = entry.value
            val entryPropertyMap = mappingConfig.propertyMapReference?.let { propertyMapFor(it) }
            if (entryPropertyMap != null) {
                properties.optJSONArray(mappingConfig.name)?.let { hierarchical ->
                    remapArrayAndTransform(hierarchical, entryPropertyMap)?.let { items ->
                        out.put(targetKey, items)
                    }
                }
            } else {
                properties.optJSONArray(mappingConfig.name)?.let { value ->
                    // Need a deep copy as we otherwise will overwrite values that could be used by other binding
                    if (mappingConfig.transforms == null && mappingConfig.operators == null) {
                        out.put(targetKey, value)
                    } else {
                        var transformed = JSONArray(value.toString())
                        mappingConfig.transforms?.forEach { transformer ->
                            (0 until transformed.length()).forEach { index ->
                                transformed.optString(index)?.let {
                                    transformed.put(index, transformers[transformer]?.transform(it)
                                            ?: it)
                                }
                            }
                        }
                        mappingConfig.operators?.forEach { operator ->
                            (0 until transformed.length()).forEach { index ->
                                transformed.optString(index)?.let {
                                    transformed.put(index, operators[operator.type]?.modify(it, operator.params)
                                            ?: it)
                                }
                            }
                        }
                        out.put(targetKey, transformed)
                    }
                }
            }

        }
        // Piggyback the internal description
        out.put(PropertyObject.INTERNAL_DESCRIPTION, flat(out, propertyMap))
        return out
    }

    private fun flat(properties: JSONObject, propertyMap: Map<String, PropertyConfig>): JSONObject {
        val out = JSONObject()
        propertyMap.forEach { (key, propertyConfig) ->
            properties.optJSONArray(key)?.let {
                out.put(key, toCommaSeparated(it, propertyConfig, typeDescriptionTemplate[propertyConfig.propertyMapReference]))
            }
        }
        return out
    }

    private fun toCommaSeparated(array: JSONArray, propertyConfig: PropertyConfig, typeDescription: String?): String? {
        val list = mutableListOf<Any>()
        for (i in 0 until array.length()) {
            list.add(array[i])
        }
        if (list.size == 0) {
            return null
        }
        return list.joinToString {
            if (it is String) {
                return@joinToString it
            }
            if (it is JSONObject) {
                if (typeDescription != null) {
                    return@joinToString CheapMustache.resolve(typeDescription, it)
                }
                else {
                    return@joinToString propertyConfig.propertyMapReference ?: "UNDEFINED_TYPE"
                }
            }
            else {
                return@joinToString "UNEXPECTED_TYPE"
            }
        }
    }

    private fun remapArrayAndTransform(items: JSONArray, propertyMap: Map<String, PropertyConfig>): JSONArray? {
        (0 until items.length()).mapNotNull {
            items.optJSONObject(it)
        }.map {
            remapAndTransform(it, propertyMap)
        }.let {
            val array = JSONArray()
            it.forEach {
                array.put(it)
            }
            return array
        }
    }
}

interface Transformer {
    fun transform(input: String): String
}

interface Operator {
    fun modify(input: String, parameters: Map<String, String>?): String
}