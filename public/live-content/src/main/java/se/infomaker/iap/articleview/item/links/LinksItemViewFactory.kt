package se.infomaker.iap.articleview.item.links

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.util.UI.mapSubViews
import se.infomaker.iap.articleview.view.BinderProvider
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableLinearLayout
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.section.detail.openExternalLink

class LinksItemViewFactory : ItemViewFactory {
    override fun typeIdentifier(): Any {
        return LinksItem::class.java
    }

    companion object {
        private val PADDING_HORIZONTAL = "PaddingHorizontal"
        private val PADDING_TOP = "PaddingTop"
        private val PADDING_BOTTOM = "PaddingBottom"
        private val PADDING_VERTICAL = "PaddingVertical"
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        var identifier = resourceManager.getLayoutIdentifier("link_item")
        if (identifier == 0) {
            identifier = R.layout.default_link_item
        }

        val view = LayoutInflater.from(parent.context).inflate(identifier, parent, false)
        view.setTag(R.id.holder, LinksViewHolder(view))
        view.mapSubViews()
        return view
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        var prefix = ""
        if (item is LinksItem) {
            val link = firstSupported(item)
            link?.themePrefix?.let {
                prefix = it
            }
            if (link != null) {
                val themeKeys = listOf("${link.themePrefix}LinkItem", "linkItem")
                view.getViewHolder().text?.setThemeKeys(themeKeys)
                view.getViewHolder().icon?.setThemeTintColor(themeKeys)
                view.getViewHolder().icon?.setThemeKey(themeKeys)

                (view as? ThemeableLinearLayout)?.setThemeTouchColor("interaction")
            } else {
                val themeKeys = listOf("disabledLinkItem", "linkItem")
                view.getViewHolder().text?.setThemeKeys(themeKeys)
                view.getViewHolder().icon?.setThemeTintColor(themeKeys)

                (view as? ThemeableLinearLayout)?.setThemeTouchColor("disabledInteraction")
            }
        }

        val horizontal = theme.getSize(ThemeSize.DEFAULT, prefix + "LinkItem" + PADDING_HORIZONTAL, "linkItem$PADDING_HORIZONTAL").sizePx.toInt()
        val top = theme.getSize(ThemeSize.DEFAULT, "${prefix}LinkItem$PADDING_TOP", "linkItem$PADDING_TOP", "${prefix}LinkItem$PADDING_VERTICAL", "linkItem$PADDING_VERTICAL").sizePx.toInt()
        val bottom = theme.getSize(ThemeSize.DEFAULT, "${prefix}LinkItem$PADDING_BOTTOM", "linkItem$PADDING_BOTTOM", "${prefix}LinkItem$PADDING_VERTICAL", "linkItem$PADDING_VERTICAL").sizePx.toInt()
        view.setPadding(horizontal, top, horizontal, bottom)

        view.getViewHolder().icon?.let { icon ->
            val size = theme.getSize("linkItemImagePaddingTop", null)
            if (size != null && view is LinearLayoutCompat) {
                val layoutParams = icon.layoutParams as LinearLayoutCompat.LayoutParams
                layoutParams.gravity = Gravity.TOP
                layoutParams.topMargin = size.sizePx.toInt()
                icon.layoutParams = layoutParams
            }
        }

        theme.apply(view)
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        if (item is LinksItem) {
            val link = firstSupported(item)
            val holder = view.getViewHolder()
            holder.text?.text = item.title
            val propertyObject = item.propertyObject
            bindProperties(view, moduleId, propertyObject)
            if (propertyObject?.externalLink != null) {
                view.setOnClickListener { view.context.openExternalLink(propertyObject) }
            }
            else if (link != null && LinksHandlerManager.canHandle(link)) {
                view.setOnClickListener { LinksHandlerManager.open(view.context, moduleId, link, item.title) }
            } else {
                view.setOnClickListener(null)
            }
        }
    }

    private fun bindProperties(view: View, moduleId: String, propertyObject: PropertyObject?) {
        if (propertyObject != null) {
            (view.getTag(R.id.viewMap) as? Map<String, View>)?.let { viewMap ->
                BinderProvider.binder(view.context, moduleId).bind(propertyObject, viewMap.values.toList())
            }
        }
    }

    private fun firstSupported(links: LinksItem): Link? {
        return links.links.firstOrNull { LinksHandlerManager.canHandle(it) }
    }
}

class LinksViewHolder(view: View) {
    val icon: ThemeableImageView? = view.findViewById(android.R.id.icon)
    val text: ThemeableTextView? = view.findViewById(android.R.id.text1)
}

fun View.getViewHolder(): LinksViewHolder = getTag(R.id.holder) as LinksViewHolder