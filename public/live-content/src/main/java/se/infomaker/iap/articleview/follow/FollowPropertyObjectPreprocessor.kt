package se.infomaker.iap.articleview.follow

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.follow.extensions.forEach

class FollowPropertyObjectPreprocessor : Preprocessor {
    override fun process(content: ContentStructure, configJson: String, resourceProvider: ResourceProvider): ContentStructure {
        val config = Gson().fromJson(configJson, FollowPropertyObjectPreprocessorConfig::class.java)
        content.properties.optJSONArray(config.propertyKey)?.forEach { properties ->
            val item = FollowPropertyObjectItemFactory.create(properties, config)
            item.listeners.add(FollowPropertyUpdateListener(item))
            content.body.items.add(item)
        }
        return content
    }
}