package se.infomaker.iap.articleview.offline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.template.TemplateItem
import se.infomaker.iap.articleview.util.UI.mapSubViews
import timber.log.Timber

class TemplateItemLoader(val loader: HeadlessArticleLoader) : ItemLoader<TemplateItem>{
    override suspend fun loadItem(context: Context, item: TemplateItem, resourceManager: ResourceManager) {

        val identifier = resourceManager.getLayoutIdentifier(item.template)
        if (identifier == 0) {
            Timber.w("Unable to find layout")
            return
        }
        val view = LayoutInflater.from(context).inflate(identifier, null, false)
        view.mapSubViews()
        val viewMap = view.getTag(R.id.viewMap) as Map<String, View>
        viewMap.entries.filter { item.boundViews.contains(it.key) }.forEach { (name, view) ->
            item.items.values.forEach { subItem ->
                loader.load(context, subItem, resourceManager)
            }
        }
    }

}
