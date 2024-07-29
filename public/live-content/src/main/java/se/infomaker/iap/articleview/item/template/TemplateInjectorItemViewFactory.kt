package se.infomaker.iap.articleview.item.template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.util.UI.mapSubViews
import se.infomaker.iap.articleview.view.BinderProvider
import se.infomaker.iap.theme.Theme
import timber.log.Timber

class TemplateInjectorItemViewFactory(private val template: String) : ItemViewFactory {

    private val typeIdentifier by lazy { TemplateInjectorItem.createTypeIdentifier(template) }

    override fun typeIdentifier() = typeIdentifier

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        val identifier = resourceManager.getLayoutIdentifier(template)
        val view = if (identifier > 0) {
            LayoutInflater.from(parent.context).inflate(identifier, parent, false)
        } else {
            Timber.e("Failed to create view with layout $template")
            View(parent.context)
        }
        view.mapSubViews()
        return view
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        val viewMap = view.getTag(R.id.viewMap) as Map<String, View>
        (item as? TemplateInjectorItem)?.let {
            BinderProvider.binder(view.context, moduleId).bind(it.propertyObject, viewMap.values.toList(), null)
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        theme.apply(view)
    }
}