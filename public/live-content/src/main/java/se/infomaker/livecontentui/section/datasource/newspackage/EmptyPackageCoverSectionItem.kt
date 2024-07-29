package se.infomaker.livecontentui.section.datasource.newspackage

import se.infomaker.iap.articleview.item.author.DividerDecorationConfig
import se.infomaker.livecontentmanager.parser.PropertyObject
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionItem

class EmptyPackageCoverSectionItem(
    propertyObject: PropertyObject,
    sectionIdentifier: String,
    groupKey: String,
    overlayThemeFile: String?
) : PropertyObjectSectionItem(propertyObject, sectionIdentifier, groupKey) {

    init {
        overlayThemeFile?.let {
            setOverlayThemes(mutableListOf(it))
        }
    }

    override fun defaultTemplate() = R.layout.package_cover_empty_default

    override fun isClickable() = false

    override fun getDividerConfig(): DividerDecorationConfig = SectionItem.NO_DIVIDER_CONFIG

    override fun createDetailView(moduleId: String?) = null

    override fun getContentTracker(moduleId: String?) = null

}