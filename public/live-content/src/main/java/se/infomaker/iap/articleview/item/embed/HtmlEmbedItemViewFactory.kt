package se.infomaker.iap.articleview.item.embed

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.updatePadding
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.connectivity.hasInternetConnection
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.ktx.suffixItems
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView

class HtmlEmbedItemViewFactory : ItemViewFactory {

    override fun typeIdentifier(): Any {
        return HtmlEmbedItem::class.java
    }

    companion object {
        val LAYOUT_PARAMS_MATCH_WRAP = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        val LAYOUT_PARAMS_MATCH_MATCH = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        return HtmlEmbedItemView(parent.context).apply {
            tag = "embed"
            layoutParams = LAYOUT_PARAMS_MATCH_WRAP
        }
    }

    private fun applyDefaultTextStyle(themeableTextView: ThemeableTextView) {
        themeableTextView.setTextColor(Color.argb(255, 127, 127, 127))
        themeableTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        view.findViewWithTag<ThemeableTextView>("offlineText")?.apply {

            theme.getText("htmlEmbedPlaceholderText", null)?.let {
                themeKeys = listOf("htmlEmbedPlaceholderText")
            } ?: applyDefaultTextStyle(this)

            theme.getColor("htmlEmbedPlaceholderBackground", null)?.let {
                this.themeBackgroundColor = "htmlEmbedPlaceholderBackground"
            } ?: this.setBackgroundColor(Color.argb(64, 204, 204, 204))

            apply(theme)
        }

        view.findViewWithTag<ThemeableImageView>("icon")?.apply {
            this.themeKey = "embedFullscreenIcon"
            this.setThemeTintColor("embedFullscreenIcon")
            apply(theme)
        }

        view.findViewWithTag<ThemeableTextView>("linkText")?.apply {
            this.themeKeys = listOf("htmlEmbedText")
            apply(theme)
        }

        view.findViewWithTag<ThemeableTextView>("openText")?.apply {
            this.themeKeys = listOf("htmlEmbedText")
            apply(theme)
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        val embed = item as HtmlEmbedItem
        view.findViewWithTag<HtmlEmbedItemView>("embed")?.apply {
            this.moduleId = moduleId
            configureWebView(item, this, width, context)
            if (!view.context.hasInternetConnection()) {
                (view as HtmlEmbedItemView).enableOfflineMode()
            } else {
                (view as HtmlEmbedItemView).enableOnlineMode()

                when (item.linkType) {
                    "external" -> {
                        val embedLinkSettings = EmbedLinkSettings(embed.baseUrl
                                ?: "", embed.data, embed.linkText ?: "External Link")
                        this.loadExternalLink(embedLinkSettings)
                        this.hide()
                        this.invalidate()
                    }
                    "internal" -> {
                        this.show()
                        this.updateTouchHandler("internal")
                    }
                    "internalLink" -> {
                        this.show()
                        val embedLinkSettings = EmbedLinkSettings(embed.baseUrl ?: "", item.data)
                        this.updateTouchHandler("internalLink", embedLinkSettings)
                    }
                }
            }
        }
    }

    private fun configureWebView(item: HtmlEmbedItem, view: HtmlEmbedItemView, width: Int, context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val customHeight = item.heightForWidth((if (width == 0) display.width else width),
                item.size ?: HtmlEmbedItem.DEFAULT_ASPECT_RATIO)
        view.setCustomHeight(customHeight)
    }
}