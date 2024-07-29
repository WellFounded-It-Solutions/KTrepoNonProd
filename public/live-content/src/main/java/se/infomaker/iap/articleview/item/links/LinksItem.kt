package se.infomaker.iap.articleview.item.links

import se.infomaker.iap.articleview.item.Item
import se.infomaker.livecontentmanager.parser.PropertyObject
import java.util.UUID

data class LinksItem(val attributes: Map<String, String>, val links: List<Link>) : Item(attributes["uuid"] ?: UUID.randomUUID().toString()) {
    override val typeIdentifier = LinksItem::class.java
    val type = attributes["type"] ?: ""
    override val selectorType = "link"
    val title = attributes["title"] ?: ""

    var propertyObject: PropertyObject? = null

    override val matchingQuery = mapOf<String, String>()
}
