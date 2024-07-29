package se.infomaker.livecontentui.section.binding

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder
import se.infomaker.livecontentui.section.ads.AdSectionItem

class AdSectionItemBinder : SectionItemBinder {
    override fun bind(item: SectionItem, viewHolder: SectionItemViewHolder, resourceManager: ResourceManager, theme: Theme): Set<LiveBinding>? {
        (item as? AdSectionItem)?.let {
            return it.bindAd(viewHolder, resourceManager, theme)
        }
        return null
    }
}