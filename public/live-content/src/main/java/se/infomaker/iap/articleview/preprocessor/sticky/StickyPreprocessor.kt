package se.infomaker.iap.articleview.preprocessor.sticky

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.preprocessor.select.Selector

class StickyPreprocessor : Preprocessor {

    private val gson by lazy { Gson() }

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        gson.fromJson(config, StickyPreprocessorConfig::class.java)?.let {
            return process(content, it)
        }
        return content
    }

    private fun process(content: ContentStructure, config: StickyPreprocessorConfig): ContentStructure {
        val items = content.body.items
        Selector.getIndexes(items, config.select).let { itemIndexes ->
            itemIndexes.forEach { items[it].sticky = true }
        }
        return content
    }
}