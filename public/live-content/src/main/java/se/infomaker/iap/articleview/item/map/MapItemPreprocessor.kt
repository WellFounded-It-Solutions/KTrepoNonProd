package se.infomaker.iap.articleview.item.map

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor

class MapItemPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val mapConfig = Gson().fromJson(config, MapItemPreprocessorConfig::class.java)
        val ratio: String = mapConfig.aspectRatio
        val interactivity: String = mapConfig.interaction

        content.body.items
            .filterIsInstance<MapItem>()
            .forEach { mapItem ->
                with(mapItem) {
                    aspectRatio = ratio
                    interaction = interactivity
                }
            }
        return content
    }
}