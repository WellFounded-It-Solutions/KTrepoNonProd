package se.infomaker.livecontentui.section.binding

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder

class PropertyObjectSectionItemBinder(val binder: PropertyBinder) : SectionItemBinder {

    override fun bind(item: SectionItem, viewHolder: SectionItemViewHolder, resourceManager: ResourceManager, theme: Theme): Set<LiveBinding>? {
        return item.bind(binder, viewHolder, resourceManager, theme)
    }
}