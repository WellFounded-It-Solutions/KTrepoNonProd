package se.infomaker.iap.articleview.item.mergedelement

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.item.TextItem
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.span.ThemeableSpan
import se.infomaker.iap.theme.transforms.ThemeTransforms
import se.infomaker.iap.theme.view.ThemeableTextView


class MergedElementItemViewFactory : ItemViewFactory {
    override fun typeIdentifier(): Any {
        return MergedElementItem::class.java
    }

    companion object {
        const val PADDING_HORIZONTAL = "PaddingHorizontal"
        const val PADDING_VERTICAL = "PaddingVertical"
        private const val PADDING_TOP = "PaddingTop"
        private const val PADDING_BOTTOM = "PaddingBottom"

        private const val MARGIN_HORIZONTAL = "MarginHorizontal"
        private const val MARGIN_VERTICAL = "MarginVertical"
        private const val MARGIN_TOP = "MarginTop"
        private const val MARGIN_BOTTOM = "MarginBottom"
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        return ThemeableTextView(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        if (item is MergedElementItem && view is ThemeableTextView) {
            view.apply(item, theme)

            val themeKeys = item.themeKeys.plus(listOf("element", "default"))

            val horizontalPadding = UI.dp2px(theme.getSize(themeKeys.suffixItems(PADDING_HORIZONTAL), ThemeSize.DEFAULT).size).toInt()

            val verticalPaddingKeys = themeKeys.suffixItems(PADDING_VERTICAL)
            val topPaddingKeys = themeKeys.suffixItems(PADDING_TOP).zip(verticalPaddingKeys).flatMap { listOf(it.first, it.second) }
            val bottomPaddingKeys = themeKeys.suffixItems(PADDING_BOTTOM).zip(verticalPaddingKeys).flatMap { listOf(it.first, it.second) }

            val topPadding = UI.dp2px(theme.getSize(topPaddingKeys, ThemeSize.DEFAULT).size).toInt()
            val bottomPadding = UI.dp2px(theme.getSize(bottomPaddingKeys, ThemeSize.DEFAULT).size).toInt()

            view.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding)

            val horizontalMargin = UI.dp2px(theme.getSize(themeKeys.suffixItems(MARGIN_HORIZONTAL), ThemeSize.ZERO).size).toInt()

            val verticalMarginKeys = themeKeys.suffixItems(MARGIN_VERTICAL)
            val topMarginKeys = themeKeys.suffixItems(MARGIN_TOP).zip(verticalMarginKeys).flatMap { listOf(it.first, it.second) }
            val bottomMarginKeys = themeKeys.suffixItems(MARGIN_BOTTOM).zip(verticalMarginKeys).flatMap { listOf(it.first, it.second) }

            val topMargin = UI.dp2px(theme.getSize(topMarginKeys, ThemeSize.ZERO).size).toInt()
            val bottomMargin = UI.dp2px(theme.getSize(bottomMarginKeys, ThemeSize.ZERO).size).toInt()

            val params = view.layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                params.setMargins(horizontalMargin, topMargin, horizontalMargin, bottomMargin)
                view.layoutParams = params
            }
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        if (item is MergedElementItem && view is ThemeableTextView) {
            view.setText(item.text, TextView.BufferType.SPANNABLE)
        }
    }
}

private fun List<String?>.suffixItems(suffix: String): List<String> = this.filterNotNull().map { it + suffix }

private fun ThemeableTextView.apply(item: MergedElementItem, theme: Theme) {
    val stringBuilder = SpannableStringBuilder()

    item.items.joinTexts(stringBuilder, separator = item.separator, lastSeparator = item.lastSeparator) {
        val transforms = theme.getText(null, *it.themeKeys.toTypedArray())?.getTransforms(theme) ?: ThemeTransforms.DEFAULT
        val s = it.text
        val spans = s.getSpans(0, s.length, Object::class.java)
        var text = s.toString()
        if (ThemeTransforms.Transforms.UPPERCASE in transforms.transforms) {
            text = text.uppercase()
        }

        return@joinTexts SpannableString(text).apply {
            spans.forEach { span ->
                val spanStart = s.getSpanStart(span)
                val spanEnd = s.getSpanEnd(span)
                if(spanStart != -1 && spanEnd != -1 && spanStart >= spanEnd) {
                    setSpan(span, spanStart, spanEnd, 0)
                }
            }
        }
    }

    item.applySpans(stringBuilder, 0, theme, item.themeKeys, listOfNotNull(item.separatorThemeKey) + item.themeKeys, outerElementSeparator = item.separator)
    this.text = stringBuilder
}

private val TextItem.separatorThemeKey: String?
    get() = (this as? MergedElementItem)?.separatorThemeKey

private fun TextItem.applySpans(
    spannable: Spannable,
    spanStart: Int,
    theme: Theme,
    overrideThemeKeys: List<String>,
    overrideSeparatorThemeKeys: List<String>,
    elementSeparator: String = "",
    outerElementSeparator: String = ""
): Int {
    if (this is MergedElementItem) {
        var itemSpanStart = spanStart
        items.forEachIndexed { index, textItem ->
            val itemSeparator = when (index) {
                items.size - 1 -> outerElementSeparator
                items.size - 2 -> lastSeparator
                else -> separator
            }
            val itemThemeKeys = overrideThemeKeys + themeKeys
            val itemSeparatorThemeKeys = overrideSeparatorThemeKeys + listOfNotNull(separatorThemeKey)
            itemSpanStart = textItem.applySpans(spannable, itemSpanStart, theme, itemThemeKeys, itemSeparatorThemeKeys, itemSeparator)
        }
        return itemSpanStart
    }
    else {
        val elementThemeKeys = overrideThemeKeys + themeKeys
        val spanElementEnd = spanStart + text.length
        spannable.setSpan(ThemeableSpan(theme, elementThemeKeys), spanStart, spanElementEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        theme.getText(elementThemeKeys, null)?.getTransforms(theme)?.apply((spannable as SpannableStringBuilder), spanStart, spanElementEnd)
        val separatorThemeKeys = overrideSeparatorThemeKeys + listOfNotNull(separatorThemeKey) + elementThemeKeys
        val spanEnd = spanElementEnd + elementSeparator.length
        if (spanEnd != spanElementEnd && spanEnd <= spannable.length) {
            spannable.setSpan(ThemeableSpan(theme, separatorThemeKeys), spanElementEnd, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }

        return spanEnd
    }
}