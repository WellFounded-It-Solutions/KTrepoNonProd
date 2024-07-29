package se.infomaker.livecontentui.section.binding

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder

class SectionItemBinderCollection (val default: SectionItemBinder) : SectionItemBinder{
    private val binders = mutableMapOf<Class<out SectionItem>, SectionItemBinder>()

    fun register(clazz: Class<out SectionItem>, binder: SectionItemBinder) {
        binders[clazz] = binder
    }

    fun registerAll(records: Map<Class<out SectionItem>, SectionItemBinder>) {
        records.forEach { (clazz, binder) ->
            register(clazz, binder)
        }
    }

    private fun getBinder(clazz: Class< out SectionItem>): SectionItemBinder {
        return binders[clazz] ?: default
    }

    override fun bind(item: SectionItem, viewHolder: SectionItemViewHolder, resourceManager: ResourceManager, theme: Theme): Set<LiveBinding>? {
        return getBinder(item.javaClass).bind(item, viewHolder, resourceManager, theme)
    }
}