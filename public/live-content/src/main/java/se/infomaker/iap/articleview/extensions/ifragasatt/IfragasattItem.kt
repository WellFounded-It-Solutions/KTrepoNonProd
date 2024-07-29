package se.infomaker.iap.articleview.extensions.ifragasatt

import se.infomaker.iap.articleview.item.Item

data class IfragasattItem(val id: String, val commentUrl: String, var commentCount: Int? = null) : Item(id) {
    override val matchingQuery = mapOf<String, String>()
    override val typeIdentifier = IfragasattItem::class.java
    override val selectorType = "ifragasatt"
    var uri: String? = null
}