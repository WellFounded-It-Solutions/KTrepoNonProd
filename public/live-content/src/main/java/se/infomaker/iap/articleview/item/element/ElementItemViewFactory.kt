package se.infomaker.iap.articleview.item.element

import android.graphics.Color
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.getSpans
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.ktx.suffixItems
import se.infomaker.iap.articleview.util.UI.dp2px
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.Themeable

class ElementItemViewFactory : ItemViewFactory {
    override fun typeIdentifier(): Any {
        return ElementItem::class.java
    }

    companion object {
        val PADDING_HORIZONTAL = "PaddingHorizontal"
        val PADDING_VERTICAL = "PaddingVertical"
        val PADDING_TOP = "PaddingTop"
        val PADDING_BOTTOM = "PaddingBottom"
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        val textView = se.infomaker.iap.theme.view.ThemeableTextView(parent.context)
        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return textView
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        if (item is ElementItem && view is se.infomaker.iap.theme.view.ThemeableTextView) {
            theme.getColor("link", null)?.let { view.setLinkTextColor(it.get()) }
            view.themeKeys = item.themeKeys
            view.apply(theme)

            val horizontalPadding = dp2px(theme.getSize(item.themeKeys.suffixItems(PADDING_HORIZONTAL), ThemeSize.DEFAULT).size).toInt()

            val verticalPaddingKeys = item.themeKeys.suffixItems(PADDING_VERTICAL)
            val topPaddingKeys = item.themeKeys.suffixItems(PADDING_TOP).zip(verticalPaddingKeys).flatMap { listOf(it.first, it.second) }
            val bottomPaddingKeys = item.themeKeys.suffixItems(PADDING_BOTTOM).zip(verticalPaddingKeys).flatMap { listOf(it.first, it.second) }

            val topPadding = dp2px(theme.getSize(topPaddingKeys, ThemeSize.DEFAULT).size).toInt()
            val bottomPadding = dp2px(theme.getSize(bottomPaddingKeys, ThemeSize.DEFAULT).size).toInt()

            view.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding)
            view.setBackgroundColor(Color.TRANSPARENT)

            (view.text as? Spanned)?.getSpans<Themeable>()?.forEach {
                it.apply(theme)
            }
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        val params = view.layoutParams
        if (params is ViewGroup.MarginLayoutParams) {
            params.setMargins(0, 0, 0, 0)
            view.layoutParams = params
        }
        if (item is ElementItem && view is se.infomaker.iap.theme.view.ThemeableTextView) {
            view.setText(item.text, TextView.BufferType.SPANNABLE)
            view.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}