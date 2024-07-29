package se.infomaker.iap.articleview.item.mergedelement

import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.TextItem
import se.infomaker.iap.articleview.preprocessor.select.Selector
import se.infomaker.iap.articleview.preprocessor.select.move.Insert
import se.infomaker.iap.articleview.preprocessor.select.move.Insert.Companion.getInsertPosition


class ElementMergerPreprocessor : Preprocessor {
    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val mergerConfig = Insert.registerInsertTypeAdapter().create().fromJson(config, ElementMergerPreprocessorConfig::class.java)
        return process(content, mergerConfig)
    }

    private fun process(content: ContentStructure, config: ElementMergerPreprocessorConfig): ContentStructure {
        val items = content.body.items

        val itemIndexes = config.selectors.flatMap { Selector.getIndexes(items, it) }
        val insertPosition = items.getInsertPosition(itemIndexes, config.insert)

        if (insertPosition !in 0..items.size) {
            config.insert.fallback?.let {
                return process(content, ElementMergerPreprocessorConfig(
                    selectors = config.selectors,
                    insert = it,
                    themeKey = config.themeKey,
                    separator = config.separator,
                    type = config.type
                ))
            }
            return content
        }

        val textItems = itemIndexes.distinct().mapNotNull { items[it] as? TextItem }
        if (textItems.isNotEmpty()) {
            val merged = MergedElementItem(textItems, config.separator, config.lastSeparator, config.themeKeys, config.separatorThemeKey, config.type)
            items.add(insertPosition, merged)
            items.removeAll(textItems)
        }

        return content
    }
}