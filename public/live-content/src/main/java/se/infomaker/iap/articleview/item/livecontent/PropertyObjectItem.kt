package se.infomaker.iap.articleview.item.livecontent

import se.infomaker.iap.articleview.item.Item
import se.infomaker.livecontentmanager.parser.PropertyObject

data class PropertyObjectItem(val propertyObject: PropertyObject, val template: String, override val selectorType: String) : Item(propertyObject.id) {
    companion object {
        fun createTemplateIdentifier(template: String): Any {
            return "${PropertyObjectItem::class.java.canonicalName}-$template"
        }
    }

    override val matchingQuery = mapOf<String, String>()
    override val typeIdentifier = createTemplateIdentifier(template)
}