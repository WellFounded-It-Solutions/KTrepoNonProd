package se.infomaker.livecontentui.section.binding

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.supplementary.ListHeaderFollowSectionItem
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder
import se.infomaker.streamviewer.view.ConceptRecord
import se.infomaker.streamviewer.view.ListHeaderFollowView

class ListHeaderFollowSectionItemBinder(private val propertyBinder: PropertyBinder, private val moduleId: String?) : SectionItemBinder {

    override fun bind(item: SectionItem, viewHolder: SectionItemViewHolder, resourceManager: ResourceManager, theme: Theme): Set<LiveBinding>? {
        (item as? ListHeaderFollowSectionItem)?.let { headerItem ->
            viewHolder.viewsArrayList.filterIsInstance(ListHeaderFollowView::class.java).firstOrNull()?.let { headerView ->
                headerView.moduleId = moduleId
                headerItem.propertyObject.optStringOrNull("name")?.let {
                    headerView.concept = ConceptRecord(headerItem.propertyObject.id, headerItem.articleProperty, it)
                }
            }
            return item.bind(propertyBinder, viewHolder, resourceManager, theme)
        }
        return null
    }
}