package se.infomaker.livecontentui.section.adapter

import se.infomaker.livecontentui.config.TemplateConfig
import se.infomaker.livecontentui.config.ThemeOverlayConfig
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionAdapterData
import se.infomaker.livecontentui.section.SectionItem

class SectionTemplatesViewBehaviour(private val themeOverlayConfig: ThemeOverlayConfig?, templates: Map<String, TemplateConfig>) : SectionViewBehaviour {

    private val viewTypeMapper = ViewTypeMapper(templates)
    private val themes = mutableMapOf<String, String>()

    override fun update(sectionAdapterData: SectionAdapterData) {
        viewTypeMapper.update(sectionAdapterData)
        update(sectionAdapterData.items)
    }

    override fun update(sectionItems: List<SectionItem>) {
        themeOverlayConfig?.let { overlayConfig ->
            sectionItems.filterIsInstance(PropertyObjectSectionItem::class.java).forEach { item ->
                overlayConfig.getOverlayThemeFile(item.propertyObject)?.let {
                    themes[item.id] = it
                }
            }
        }
    }

    override fun viewTypeForKey(key: SectionItem) = viewTypeMapper.getViewType(key)

    override fun layoutResourceForViewType(viewType: Int) = viewTypeMapper.getLayoutResource(viewType)

    override fun bindingOverridesForViewType(viewType: Int) = viewTypeMapper.getTemplateConfig(viewType)?.bindingOverrides

    override fun themesForKey(key: SectionItem): List<String>? {
        val itemThemes = key.overlayThemes()?.toMutableList() ?: mutableListOf()
        themes[key.id]?.let {
            itemThemes.add(it)
        }
        return itemThemes.toSet().toList().ifEmpty { null }
    }
}