package se.infomaker.iap.articleview.item.template

import se.infomaker.iap.articleview.item.Item

data class TemplateItem(override val id: String,
                        override val template: String,
                        override val items: Map<String, Item>,
                        override val boundViews: List<String>): BaseTemplateItem, Item(id) {
    companion object {
        fun createTemplateIdentifier(template: String): Any {
            return "${TemplateItem::class.java.canonicalName}-$template"
        }
    }

    override val typeIdentifier = createTemplateIdentifier(template)
    override val selectorType = "template"
    override val matchingQuery = mapOf("template" to template)
}