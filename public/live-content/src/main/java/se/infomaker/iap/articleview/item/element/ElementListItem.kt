package se.infomaker.iap.articleview.item.element

import se.infomaker.iap.articleview.item.Item

data class ElementListItem(val id: String,
                           val themeKeys: List<String>,
                           val indicatorThemeKeys: List<String>,
                           val attributes: Map<String, String>,
                           val elementItems: List<ElementItem>,
                           val listType: ListType,
                           val variation: String? = attributes["variation"]) : Item(id) {

    override val matchingQuery = mapOf("listType" to listType.name.toLowerCase())
    override val typeIdentifier = ElementListItem::class.java
    override val selectorType = "elementList"

    enum class ListType {
        ORDERED, UNORDERED
    }
}