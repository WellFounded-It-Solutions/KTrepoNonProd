package se.infomaker.iap.articleview.item.links

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor

class TextHtmlLinkHandler : LinkHandler{
    override fun open(context: Context, moduleId: String, link: Link, title: String) {
        val theme = ThemeManager.getInstance(context).appTheme
        val builder = CustomTabsIntent.Builder()
        val primary = theme.getColor("primary", ThemeColor.TRANSPARENT)
        val color = if (primary === ThemeColor.TRANSPARENT) Color.GRAY else primary.get()
        builder.setToolbarColor(color)
        val customTabsIntent = builder.build()
        link.attributes["url"]?.let { customTabsIntent.launchUrl(context, Uri.parse(it)) }
    }
}