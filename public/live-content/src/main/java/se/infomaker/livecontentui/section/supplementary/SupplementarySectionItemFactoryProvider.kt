package se.infomaker.livecontentui.section.supplementary

import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.section.SectionItem

data class FactoryKey(val contentType: String, val type: SupplementarySectionItemType, val supplementaryItemContentType: String)

typealias Factory = (PropertyObject, LiveContentConfig) -> SectionItem?

object SupplementarySectionItemFactoryProvider {
    private val factories = mutableMapOf<FactoryKey, Factory>()

    init {
        register(FactoryKey("List", SupplementarySectionItemType.FOOTER, "Concept")) { concept, liveContentConfig ->
            liveContentConfig.conceptTypeUuidsMap?.let { mapping ->
                (mapping[concept.conceptType] ?: mapping["default"])?.let { articleProperty ->
                    ListFooterSectionItem(concept, articleProperty)
                }
            }
        }
    }

    fun register(key: FactoryKey, factory: Factory) {
        factories[key] = factory
    }

    fun provide(key: FactoryKey) = factories[key]
}