package se.infomaker.iap.articleview.preprocessor.propertyelement

import android.text.SpannableStringBuilder
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.preprocessor.reproducibleUuid
import se.infomaker.livecontentmanager.parser.PropertyObject

class PropertyElementPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {

        val propertyElementPreprocessorConfig = Gson().fromJson(config, PropertyElementPreprocessorConfig::class.java)

        propertyElementPreprocessorConfig?.properties?.forEach { propertyElementConfig ->
            val keyPath = propertyElementConfig.property.split(".")
            content.properties.optStringsAt(keyPath).forEachIndexed { index, value ->
                val propUuid = JSONUtil.optJSONArray(content.properties, "contentId")?.get(0) as? String ?: PropertyObject.NO_UUID
                val id = "$propUuid:${propertyElementConfig.reproducibleUuid}:$index"
                content.body.items.add(ElementItem(id, propertyElementConfig.themeKeys, mapOf(Pair("type", propertyElementConfig.type)), SpannableStringBuilder(value)))
            }
        }
        return content
    }
}

private fun JSONObject.optStringsAt(keyPath: List<String>): List<String> {
    val out = mutableListOf<String>()
    keyPath.forEachIndexed { index, key ->
        when (val value = opt(key)) {
            is JSONObject -> out.addAll(value.optStringsAt(keyPath.subList(index, keyPath.size)))
            is JSONArray -> out.addAll(value.optStringsAt(keyPath.subList(index, keyPath.size)))
            else -> value?.let { out.add(it.toString()) }
        }
    }
    return out
}

private fun JSONArray.optStringsAt(keyPath: List<String>): List<String> {
    val out = mutableListOf<String>()
    for (i in 0 until length()) {
        when (val value = get(i)) {
            is JSONObject -> out.addAll(value.optStringsAt(keyPath))
            else -> value?.let { out.add(it.toString()) }
        }
    }
    return out
}

private val PropertyElementConfig.themeKeys: List<String>
    get() = listOf(type, "propertyElement", "element", "default")