package se.infomaker.livecontentui.section.supplementary

import androidx.fragment.app.Fragment
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.impressions.ContentTracker
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder

abstract class SupplementarySectionItem(
    propertyObject: PropertyObject,
    val articleProperty: String,
    sectionIdentifier: String,
    template: String,
    context: JSONObject
) : PropertyObjectSectionItem(propertyObject, sectionIdentifier, "ConceptSupplementary", template, template, context) {

    override fun bind(binder: PropertyBinder, viewHolder: SectionItemViewHolder, resourceManager: ResourceManager, theme: Theme): MutableSet<LiveBinding> {
        viewHolder.item = this
        theme.apply(viewHolder.itemView)
        return binder.bind(propertyObject, viewHolder.viewsArrayList, context)
    }

    override fun isItemTheSame(sectionItem: SectionItem?): Boolean {
        (sectionItem as? SupplementarySectionItem)?.let {
            return super.isItemTheSame(sectionItem)
        }
        return false
    }

    override fun areContentsTheSame(sectionItem: SectionItem?): Boolean {
        (sectionItem as? SupplementarySectionItem)?.let {
            return super.areContentsTheSame(sectionItem)
        }
        return false
    }

    override fun getDividerConfig(): DividerDecorationConfig = SectionItem.NO_DIVIDER_CONFIG

    override fun createDetailView(moduleId: String?) = Fragment() // Not supported

    override fun getContentTracker(moduleId: String?): ContentTracker? = null
}