package se.infomaker.iap.articleview.preprocessor.links

import org.json.JSONArray
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.follow.extensions.firstOrNull
import se.infomaker.iap.articleview.item.links.LinksItem
import se.infomaker.livecontentmanager.parser.PropertyObject

class LinkPropertiesPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        content.properties.optJSONArray(PROPERTY_KEY)?.let { linkedArticles ->
            content.body.items.filterIsInstance(LinksItem::class.java).forEach { linksItem ->
                linksItem.enrich(linkedArticles)
            }
        }
        return content
    }

    private fun LinksItem.enrich(properties: JSONArray) {
        properties.firstOrNull { it.optJSONArray("contentId")?.optString(0, null) == uuid }
            ?.let { this.propertyObject = PropertyObject(id = uuid, properties = it) }
    }

    companion object {
        private const val PROPERTY_KEY = "linkedArticles"
    }
}