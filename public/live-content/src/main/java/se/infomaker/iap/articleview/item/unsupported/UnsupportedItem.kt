package se.infomaker.iap.articleview.item.unsupported

import java.util.UUID

data class UnsupportedItem(val type: String?, val attributes: Map<String, String>) : se.infomaker.iap.articleview.item.Item(UUID.randomUUID().toString()) {
    override val typeIdentifier = UnsupportedItem::class.java
    override val selectorType = "unsupportedItem"

    override val matchingQuery = mapOf<String, String>()
}