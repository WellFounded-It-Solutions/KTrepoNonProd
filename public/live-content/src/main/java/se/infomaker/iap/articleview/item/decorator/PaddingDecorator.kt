package se.infomaker.iap.articleview.item.decorator

import android.view.View
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize

data class PaddingDecorator(val left: List<String>? = null, val top: List<String>? = null, val right: List<String>? = null, val bottom: List<String>? = null) : ItemDecorator {

    override fun decorate(item: Item, itemView: View, theme: Theme) {

        val originalPadding = itemView.originalPadding()

        itemView.setPadding(originalPadding.left + theme.getSize(left, ThemeSize.ZERO).sizePx.toInt(),
                originalPadding.top + theme.getSize(top, ThemeSize.ZERO).sizePx.toInt(),
                originalPadding.right + theme.getSize(right, ThemeSize.ZERO).sizePx.toInt(),
                originalPadding.bottom + theme.getSize(bottom, ThemeSize.ZERO).sizePx.toInt())
    }
}