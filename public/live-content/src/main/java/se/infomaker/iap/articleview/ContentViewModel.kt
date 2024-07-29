package se.infomaker.iap.articleview

import se.infomaker.iap.articleview.item.Item

/**
 * Represents the content body
 */
data class ContentViewModel(val items: MutableList<Item> = mutableListOf()) {

    fun stripItemsMatching(items: List<Item>): ContentViewModel {
        items.forEach { item ->
            this.items.removeAll {
                it.uuid == item.uuid && it == item
            }
        }
        return this
    }
}