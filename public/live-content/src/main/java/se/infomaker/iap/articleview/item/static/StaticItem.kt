package se.infomaker.iap.articleview.item.static

import se.infomaker.iap.articleview.item.Item

data class StaticItem(val id: String, val template: String, override val selectorType: String) : Item(id) {
    companion object {
        fun createTemplateIdentifier(template: String): Any = "${StaticItem::class.java.canonicalName}-$template"
    }

    override val typeIdentifier = createTemplateIdentifier(template)
    override val matchingQuery: Map<String, String> = mapOf()
}