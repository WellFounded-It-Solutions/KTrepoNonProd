package se.infomaker.iap.articleview.item.template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OneShotPreDrawListener
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.item.template.binder.TemplateBinder
import se.infomaker.iap.articleview.util.UI.mapSubViews
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.Themeable
import timber.log.Timber

open class TemplateItemViewFactory(private val template: String) : ItemViewFactory {

    override fun typeIdentifier(): Any = TemplateItem.createTemplateIdentifier(template)

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        val identifier = resourceManager.getLayoutIdentifier(template)
        val view = if (identifier > 0) {
            LayoutInflater.from(parent.context).inflate(identifier, parent, false)
        } else {
            Timber.e("Failed to create view with layout $template")
            View(parent.context)
        }

        view.mapSubViews()
        if (view is Themeable) {
            OneShotPreDrawListener.add(view) {
                view.layoutParams = (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    if (height == ViewGroup.MarginLayoutParams.MATCH_PARENT) {
                        height = view.height

                        val marginTop = theme.getSize(getThemeMarginSizeList("Top"), ThemeSize(0.toFloat())).sizePx
                        val marginBottom = theme.getSize(getThemeMarginSizeList("Bottom"), ThemeSize(0.toFloat())).sizePx
                        topMargin += marginTop.toInt()
                        bottomMargin += marginBottom.toInt()
                    }
                }
            }
        }

        val paddingTop = theme.getSize(getThemePaddingVerticalSizeList("Top"), ThemeSize.ZERO).sizePx.toInt()
        val paddingBottom = theme.getSize(getThemePaddingVerticalSizeList("Bottom"), ThemeSize.ZERO).sizePx.toInt()

        val paddingHorizontal = theme.getSize("${template}TemplatePaddingHorizontal", ThemeSize.ZERO).sizePx.toInt()

        view.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, paddingBottom)

        return view
    }

    private fun getThemeMarginSizeList(suffix: String): List<String> =
        listOf("${template}TemplateMargin$suffix", "${template}TemplateMargin")

    private fun getThemePaddingVerticalSizeList(suffix: String): List<String> =
        listOf("${template}TemplatePadding$suffix", "${template}TemplateMarginVertical")

    override fun themeView(view: View, item: Item, theme: Theme) {
        theme.apply(view)
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        val viewMap = view.getTag(R.id.viewMap) as Map<String, View>

        if (item is BaseTemplateItem) {
            viewMap.entries.filter { item.boundViews.contains(it.key) }.forEach { (name, view) ->
                TemplateBinder.bind(view, item.items[name])
            }
        }
    }
}