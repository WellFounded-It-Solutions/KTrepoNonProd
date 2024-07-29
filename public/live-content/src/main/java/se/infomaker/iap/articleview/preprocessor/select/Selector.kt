package se.infomaker.iap.articleview.preprocessor.select

import se.infomaker.iap.articleview.item.Item

object Selector {
    fun getIndexes(items: List<Item>, config: SelectorConfig): List<Int> {
        val types = config.type?.split("|")
        val filteredItems = items.mapIndexed { index, _ -> index }
                .filter { types == null || types.contains(items[it].selectorType) }
                .filter { config.matching.isEmpty() || items[it].isMatching(config.matching) }

        if (config.subset == null) { //if no subset is specified, return all items
            return filteredItems
        }
        return config.getSubset().mapNotNull {
            when (it) {
                -1 -> filteredItems.lastOrNull()
                in filteredItems.indices -> filteredItems[it]
                else -> null
            }
        }.distinct().sorted()
    }
}