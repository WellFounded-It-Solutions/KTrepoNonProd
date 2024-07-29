package se.infomaker.iap.articleview.item.prayer

import android.view.View
import android.view.ViewGroup
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.Themeable

class PrayerTimesViewFactory : ItemViewFactory {

    override fun bindView(item: Item, view: View, moduleId: String) {
        (view as? PrayerTimesView)?.bind(item as PrayerTimesItem)
    }

    override fun createView(
        parent: ViewGroup,
        resourceManager: ResourceManager,
        theme: Theme
    ): View {
        return PrayerTimesView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            this.theme = theme
            apply(theme)
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        if (item is PrayerTimesItem && view is Themeable) {
            theme.apply(view)
        }
    }

    override fun typeIdentifier() = PrayerTimesItem::class.java
}