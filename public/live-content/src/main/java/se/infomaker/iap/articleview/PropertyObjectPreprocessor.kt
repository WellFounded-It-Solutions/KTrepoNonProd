package se.infomaker.iap.articleview

import com.google.gson.Gson
import se.infomaker.iap.articleview.item.livecontent.PropertyObjectItem
import se.infomaker.livecontentmanager.parser.PropertyObject
import java.util.UUID

class PropertyObjectPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: se.infomaker.frtutilities.ResourceProvider): ContentStructure {
        val preprocessorConfig = Gson().fromJson(config, PropertyObjectPreprocessorConfig::class.java)

        content.properties.optJSONArray(preprocessorConfig.propertyKey)?.let { objects ->
            (0 until objects.length()).map { objects.getJSONObject(it) }.forEach { values ->
                val id = values.optString("contentId", "generated" + UUID.randomUUID().toString())
                content.body.items.add(PropertyObjectItem(PropertyObject(values, id) , preprocessorConfig.template, preprocessorConfig.selectorType ?: "propertyObject"))
            }
        }
        return content
    }
}