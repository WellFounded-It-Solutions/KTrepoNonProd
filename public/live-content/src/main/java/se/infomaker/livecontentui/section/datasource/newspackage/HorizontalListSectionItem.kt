package se.infomaker.livecontentui.section.datasource.newspackage

import androidx.fragment.app.Fragment
import org.json.JSONObject
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig
import se.infomaker.livecontentmanager.parser.PropertyObject
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.livecontentui.config.ContentTypeTemplateConfig
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionItemWrapper

class HorizontalListSectionItem(
    propObject: PropertyObject, sectionIdentifier: String,
    overlayThemeFiles: List<String>?,
    template: String,
    templateReference: String,
    groupKey: String?,
    private val dividerConfig: DividerDecorationConfig,
    override val config: ContentTypeTemplateConfig.Config?,
    context: JSONObject?
) : PropertyObjectSectionItem(propObject, sectionIdentifier, groupKey, template, templateReference, context), SectionItemWrapper {

    private val tracker: PropertyObjectSectionItemContentTracker =
        PropertyObjectSectionItemContentTracker(this)

    init {
        setOverlayThemes(overlayThemeFiles)
    }

    override fun defaultTemplate(): Int {
        return R.layout.section_horizontal_list_default
    }

    override fun isClickable() = false

    override fun getDividerConfig() = dividerConfig

    override fun getContentTracker(moduleId: String?) = tracker.also { it.setModuleId(moduleId) }

    override fun createDetailView(moduleId: String): Fragment = Fragment() // Currently not supported.
}