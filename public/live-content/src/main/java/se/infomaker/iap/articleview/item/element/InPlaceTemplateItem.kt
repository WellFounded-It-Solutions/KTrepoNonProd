package se.infomaker.iap.articleview.item.element

import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.template.BaseTemplateItem
import se.infomaker.iap.articleview.item.template.TemplateItem

data class InPlaceTemplateItem(override val id: String,
                          override val template: String,
                          override val items: Map<String, Item>,
                          override val boundViews: List<String>) : BaseTemplateItem, Item(id) {

    override val selectorType = "inPlaceTemplate"
    override val typeIdentifier = TemplateItem.createTemplateIdentifier(template)
    override val matchingQuery = mapOf("template" to template)
}