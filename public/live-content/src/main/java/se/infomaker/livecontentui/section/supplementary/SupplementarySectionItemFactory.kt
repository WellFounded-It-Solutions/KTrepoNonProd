package se.infomaker.livecontentui.section.supplementary

import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.extensions.optLinkedConcepts
import se.infomaker.livecontentui.section.SectionItem

enum class SupplementarySectionItemType {
    HEADER, FOOTER
}

data class SupplementarySectionItems(val header: SectionItem?, val footer: SectionItem?)

class SupplementarySectionItemFactory(private val liveContentConfig: LiveContentConfig) {
    fun create(propertyObject: PropertyObject, contentType: String): SupplementarySectionItems? {
        return propertyObject.optLinkedConcepts()?.firstOrNull()?.let { concept ->
            concept.contentType?.let { supplementaryContentType ->
                val headerFactory = SupplementarySectionItemFactoryProvider.provide(FactoryKey(contentType, SupplementarySectionItemType.HEADER, supplementaryContentType))
                val footerFactory = SupplementarySectionItemFactoryProvider.provide(FactoryKey(contentType, SupplementarySectionItemType.FOOTER, supplementaryContentType))
                SupplementarySectionItems(headerFactory?.invoke(concept, liveContentConfig), footerFactory?.invoke(concept, liveContentConfig))
            }
        }
    }
}