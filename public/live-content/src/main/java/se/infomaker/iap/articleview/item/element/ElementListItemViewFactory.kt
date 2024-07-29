package se.infomaker.iap.articleview.item.element

import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.text.getSpans
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.ktx.isVisible
import se.infomaker.iap.articleview.ktx.suffixItems
import se.infomaker.iap.articleview.util.UI.px2dp
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.Themeable

class ElementListItemViewFactory : ItemViewFactory {

    override fun typeIdentifier(): Any {
        return ElementListItem::class.java
    }

    companion object {
        const val BACKGROUND = "Background"

        const val PADDING_HORIZONTAL = "PaddingHorizontal"
        const val PADDING_VERTICAL = "PaddingVertical"
        const val PADDING_TOP = "PaddingTop"
        const val PADDING_LEFT = "PaddingLeft"
        const val PADDING_BOTTOM = "PaddingBottom"
        const val PADDING_RIGHT = "PaddingRight"

        const val MARGIN_HORIZONTAL = "MarginHorizontal"
        const val MARGIN_VERTICAL = "MarginVertical"
        const val MARGIN_TOP = "MarginTop"
        const val MARGIN_BOTTOM = "MarginBottom"
        const val MARGIN_LEFT = "MarginLeft"
        const val MARGIN_RIGHT = "MarginRight"
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        return LinearLayoutCompat(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayoutCompat.VERTICAL
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        if (item is ElementListItem && view is ViewGroup) {
            val itemCount = view.childCount

            for (i in 0 until itemCount) {
                val child = view.getChildAt(i)
                if (child is ElementListItemView) {
                    theme.getColor("link", null)?.let { child.element.setLinkTextColor(it.get()) }
                    child.element.themeKeys = item.themeKeys
                    child.element.apply(theme)

                    val horizontalIndicatorPaddingKeys = item.indicatorThemeKeys.suffixItems(PADDING_HORIZONTAL)
                    val horizontalPadding = theme.getSize(horizontalIndicatorPaddingKeys, ThemeSize(8f)).sizePx.toInt()

                    val verticalIndicatorPaddingKeys = item.indicatorThemeKeys.suffixItems(PADDING_VERTICAL)
                    val verticalPadding = theme.getSize(verticalIndicatorPaddingKeys, ThemeSize(0f)).sizePx.toInt()

                    if (child.orderedIndicator.isVisible()) {
                        child.orderedIndicator.themeKeys = item.indicatorThemeKeys
                        child.orderedIndicator.apply(theme)

                        val textToMeasure = when {
                            itemCount > 99 -> "888."
                            itemCount > 9 -> "88."
                            else -> "8."
                        }
                        val textView = child.orderedIndicator.paint.measureText(textToMeasure)
                        val viewWidth = textView.toInt() + (2 * horizontalPadding)
                        val oldParams = child.orderedIndicator.layoutParams
                        oldParams.width = viewWidth
                        child.orderedIndicator.layoutParams = oldParams
                        val orderedHorizontalPadding = Pair(0, horizontalPadding)

                        child.orderedIndicator.setPadding(orderedHorizontalPadding.first, verticalPadding, orderedHorizontalPadding.second, verticalPadding)
                    }
                    else {
                        val fontMetrics = child.element.paint.fontMetrics
                        val textHeight = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading
                        val oldLayoutParams = child.unorderedIndicator.layoutParams
                        oldLayoutParams.height = textHeight.toInt()
                        child.unorderedIndicator.layoutParams = oldLayoutParams
                        child.unorderedIndicator.apply(theme)

                        val defaultIndicatorVerticalPadding = (px2dp(textHeight) - ElementListItemView.BULLET_PREFERRED_HEIGHT) / 2
                        var unorderedDefaultVerticalPadding = ThemeSize(defaultIndicatorVerticalPadding)
                        theme.getImage(child.unorderedIndicator.themeKey, null)?.let {
                            unorderedDefaultVerticalPadding = ThemeSize(0f)
                        }
                        val unorderedVerticalPadding = theme.getSize(verticalIndicatorPaddingKeys, unorderedDefaultVerticalPadding).sizePx.toInt()

                        child.unorderedIndicator.setPadding(horizontalPadding, unorderedVerticalPadding, horizontalPadding, unorderedVerticalPadding)
                    }

                    if (i < itemCount - 1) {
                        // Apply line spacing as padding _between_ elements in the list
                        val elementBottomPadding = child.element.lineHeight - child.element.paint.getFontMetricsInt(null)
                        child.setPadding(0, 0, 0, elementBottomPadding)
                    }

                    (child.element.text as? Spanned)?.getSpans<Themeable>()?.forEach {
                        it.apply(theme)
                    }
                }
            }

            resolveAndSetPadding(view, item, theme)
            resolveAndAddMargin(view, item, theme)

            val backgroundColorKeys = item.themeKeys.suffixItems(BACKGROUND)
            view.setBackgroundColor(theme.getColor(backgroundColorKeys, ThemeColor.TRANSPARENT).get())
        }
    }

    private fun resolveAndSetPadding(view: View, item: ElementListItem, theme: Theme) {
        val horizontalPaddingKeys = item.themeKeys.suffixItems(PADDING_HORIZONTAL)
        val leftPaddingKeys = item.themeKeys.suffixItems(PADDING_LEFT).zip(horizontalPaddingKeys).flatMap { listOf(it.first, it.second) }
        val rightPaddingKeys = item.themeKeys.suffixItems(PADDING_RIGHT).zip(horizontalPaddingKeys).flatMap { listOf(it.first, it.second) }

        val leftPadding = theme.getSize(leftPaddingKeys, ThemeSize.DEFAULT).sizePx.toInt()
        val rightPadding = theme.getSize(rightPaddingKeys, ThemeSize.DEFAULT).sizePx.toInt()

        val verticalPaddingKeys = item.themeKeys.suffixItems(PADDING_VERTICAL)
        val topPaddingKeys = item.themeKeys.suffixItems(PADDING_TOP).zip(verticalPaddingKeys).flatMap { listOf(it.first, it.second) }
        val bottomPaddingKeys = item.themeKeys.suffixItems(PADDING_BOTTOM).zip(verticalPaddingKeys).flatMap { listOf(it.first, it.second) }

        val topPadding = theme.getSize(topPaddingKeys, ThemeSize.DEFAULT).sizePx.toInt()
        val bottomPadding = theme.getSize(bottomPaddingKeys, ThemeSize.DEFAULT).sizePx.toInt()

        view.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
    }

    private fun resolveAndAddMargin(view: View, item: ElementListItem, theme: Theme) {
        val horizontalMarginKeys = item.themeKeys.suffixItems(MARGIN_HORIZONTAL)
        val leftMarginKeys = item.themeKeys.suffixItems(MARGIN_LEFT).zip(horizontalMarginKeys).flatMap { listOf(it.first, it.second) }
        val rightMarginKeys = item.themeKeys.suffixItems(MARGIN_RIGHT).zip(horizontalMarginKeys).flatMap { listOf(it.first, it.second) }

        val leftMargin = theme.getSize(leftMarginKeys, ThemeSize(0f)).sizePx.toInt()
        val rightMargin = theme.getSize(rightMarginKeys, ThemeSize(0f)).sizePx.toInt()

        val verticalMarginKeys = item.themeKeys.suffixItems(MARGIN_VERTICAL)
        val topMarginKeys = item.themeKeys.suffixItems(MARGIN_TOP).zip(verticalMarginKeys).flatMap { listOf(it.first, it.second) }
        val bottomMarginKeys = item.themeKeys.suffixItems(MARGIN_BOTTOM).zip(verticalMarginKeys).flatMap { listOf(it.first, it.second) }

        val topMargin = theme.getSize(topMarginKeys, ThemeSize(0f)).sizePx.toInt()
        val bottomMargin = theme.getSize(bottomMarginKeys, ThemeSize(0f)).sizePx.toInt()

        val childParams = view.layoutParams
        if (childParams is ViewGroup.MarginLayoutParams) {
            childParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            view.layoutParams = childParams
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        if (view is LinearLayoutCompat) {
            view.removeAllViews()
            val resourceManager = ResourceManager(view.context, moduleId)
            if (item is ElementListItem) {
                item.elementItems.forEachIndexed { index, elementItem ->
                    val listItemView = ElementListItemView(view.context).apply {
                        element.text = elementItem.text
                        handleIndicator(item, resourceManager, index)
                    }
                    view.addView(listItemView)
                }
            }
        }
    }
}
