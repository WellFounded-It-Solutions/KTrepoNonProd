package se.infomaker.iap.articleview.extensions.ifragasatt

import android.view.View
import android.view.ViewGroup
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.theme.Theme

class IfragasattItemViewFactory: ItemViewFactory {
    override fun typeIdentifier(): Any = IfragasattItem::class.java

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        val view = IfragasattItemView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return view
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        theme.apply(view)
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        (view as? IfragasattItemView)?.bind(item as IfragasattItem)
    }
}