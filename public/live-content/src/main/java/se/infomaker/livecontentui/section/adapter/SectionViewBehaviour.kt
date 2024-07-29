package se.infomaker.livecontentui.section.adapter

import org.json.JSONObject
import se.infomaker.livecontentui.ViewBehaviour
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionAdapterData
import se.infomaker.livecontentui.section.SectionItem

interface SectionViewBehaviour : ViewBehaviour<SectionItem> {

    fun update(sectionAdapterData: SectionAdapterData) {
        update(sectionAdapterData.items)
    }

    fun update(sectionItems: List<SectionItem>)

    override fun presentationContextForKey(key: SectionItem): JSONObject? {
        return (key as? PropertyObjectSectionItem)?.context
    }
}