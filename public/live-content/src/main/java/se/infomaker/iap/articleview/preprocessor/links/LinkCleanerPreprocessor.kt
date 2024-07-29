package se.infomaker.iap.articleview.preprocessor.links

import org.json.JSONObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.links.LinksItem

class LinkCleanerPreprocessor : Preprocessor {

    companion object {
        const val defaultPropertyKey = "linkedArticles"
    }

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        content.properties.optJSONArray(defaultPropertyKey)?.let { linkedArticles ->
            val usableArticleUUIDs = mutableListOf<String>()
            for (i in 0 until linkedArticles.length()) {
                (linkedArticles[i] as? JSONObject)?.getJSONArray("contentId")?.getString(0)?.let {
                    usableArticleUUIDs.add(it)
                }
            }
            removeUnpublishedLinks(content.body.items, usableArticleUUIDs)
        }
        return content
    }

    private fun removeUnpublishedLinks(items: MutableList<Item>, whitelist: List<String>) {
        items.removeAll(items.filterIsInstance<LinksItem>().filter { !whitelist.contains(it.uuid) })
    }
}