package se.infomaker.livecontentui.section.binding

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder

interface SectionItemBinder {
    fun bind(item: SectionItem, viewHolder: SectionItemViewHolder, resourceManager: ResourceManager, theme: Theme): Set<LiveBinding>?
}
