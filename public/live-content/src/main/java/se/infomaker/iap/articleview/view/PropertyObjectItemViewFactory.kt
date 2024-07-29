package se.infomaker.iap.articleview.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.item.livecontent.PropertyObjectItem
import se.infomaker.iap.articleview.util.UI.mapSubViews
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentmanager.parser.PropertyObject
import timber.log.Timber

class PropertyObjectItemViewFactory(val template: String) : ItemViewFactory {

    override fun typeIdentifier(): Any = PropertyObjectItem.createTemplateIdentifier(template)

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

    override fun themeView(view: View, item: Item, theme: Theme) {
        theme.apply(view)
    }


    override fun bindView(item: Item, view: View, moduleId: String) {
        (item as? PropertyObjectItem)?.let {
            bindView(view, moduleId, it.propertyObject)
        }
    }

    fun bindView(view: View, moduleId: String, propertyObject: PropertyObject) {
        val viewMap = view.getTag(R.id.viewMap) as Map<String, View>
        BinderProvider.binder(view.context, moduleId).bind(propertyObject, viewMap.values.toList(), null)
    }
}