package se.infomaker.iap.articleview.item.template

import se.infomaker.iap.articleview.item.Item
import se.infomaker.livecontentmanager.parser.PropertyObject

data class TemplateInjectorItem(val propertyObject: PropertyObject, val template: String, override val selectorType: String): Item(propertyObject.id) {
    override val matchingQuery = mapOf("template" to template)
    override val typeIdentifier by lazy { createTypeIdentifier(template) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        (other as? TemplateInjectorItem)?.let {
            if (!propertyObject.areContentsTheSame(it.propertyObject)) return false
            if (selectorType != it.selectorType) return false
            if (template != it.template) return false
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = propertyObject.hashCode()
        result = 31 * result + selectorType.hashCode()
        result = 31 * result + template.hashCode()
        return result
    }

    companion object {
        fun createTypeIdentifier(template: String) = "${TemplateInjectorItem::class.java.canonicalName}-$template"
    }
}