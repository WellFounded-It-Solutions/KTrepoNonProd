package se.infomaker.iap.articleview.item.static

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.theme.Theme
import timber.log.Timber

class StaticItemViewFactory(private val template: String) : ItemViewFactory {
    override fun typeIdentifier(): Any = StaticItem.createTemplateIdentifier(template)

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        val identifier = resourceManager.getLayoutIdentifier(template)
        return if (identifier > 0) {
            LayoutInflater.from(parent.context).inflate(identifier, parent, false)
        } else {
            Timber.e("Failed to create view with layout $template")
            View(parent.context)
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        theme.apply(view)
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        // Bind is a no-op for templates
    }
}