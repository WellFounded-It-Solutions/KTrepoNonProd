package se.infomaker.iap.articleview.item

import android.view.View
import android.view.ViewGroup
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

/**
 * Provides functionality to create/bind/theme views representing the item
 */
interface ItemViewFactory {

    fun typeIdentifier(): Any

    /**
     * Create a view to use for the item
     */
    fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View

    /**
     * Theme the view for the item
     */
    fun themeView(view: View, item: Item, theme: Theme)

    /**
     * Bind the item to the view
     */
    fun bindView(item: Item, view: View, moduleId: String)
}