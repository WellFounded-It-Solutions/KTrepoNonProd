package se.infomaker.iap.articleview.preprocessor.select.move

import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.preprocessor.select.Selector
import se.infomaker.iap.articleview.preprocessor.select.move.Insert.Companion.getInsertPosition

class MovePreprocessor : Preprocessor {
    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val builder = Insert.registerInsertTypeAdapter()
        val moveConfig = builder.create().fromJson(config, MovePreprocessorConfig::class.java)
        return process(content, moveConfig)
    }

    private fun process(content: ContentStructure, config: MovePreprocessorConfig): ContentStructure {
        val items = content.body.items

        val itemIndexes = Selector.getIndexes(items, config.select)
        val insertPosition = items.getInsertPosition(itemIndexes, config.insert)

        if (insertPosition !in 0 until items.size) {
            config.insert.fallback?.let {
                return process(content, MovePreprocessorConfig(config.select, it))
            }
            return content
        }

        val itemList = items.pop(itemIndexes)
        items.addAll(insertPosition, itemList)
        return content
    }
}

private fun MutableList<Item>.pop(indexes: List<Int>): List<Item> {
    val items = indexes.map { this[it] }
    this.removeAll(items)
    return items
}