package se.infomaker.iap.articleview.item.decorator

import android.view.View
import android.view.ViewGroup
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize

data class MarginDecorator(val left: List<String>? = null, val top: List<String>? = null, val right: List<String>? = null, val bottom: List<String>? = null) : ItemDecorator {

    override fun decorate(item: Item, itemView: View, theme: Theme) {
        val params = itemView.layoutParams
        val marginParams = params as? ViewGroup.MarginLayoutParams ?: ViewGroup.MarginLayoutParams(params.width, params.height)

        var (left, top, right, bottom) = itemView.originalMargin(marginParams)

        val marginDecorators = (itemView.getMarginDecorators() + this)
        itemView.setMarginDecorators(marginDecorators)

        marginDecorators.forEach {
            left += theme.getSize(it.left, ThemeSize.ZERO).sizePx.toInt()
            top += theme.getSize(it.top, ThemeSize.ZERO).sizePx.toInt()
            right += theme.getSize(it.right, ThemeSize.ZERO).sizePx.toInt()
            bottom += theme.getSize(it.bottom, ThemeSize.ZERO).sizePx.toInt()
        }

        marginParams.setMargins(left, top, right, bottom)

        itemView.layoutParams = marginParams
    }
}

internal fun View.getMarginDecorators(): Set<MarginDecorator> {
    return getTag(R.id.margin_decorators) as? Set<MarginDecorator> ?: emptySet()
}

internal fun View.setMarginDecorators(marginDecorators: Set<MarginDecorator>) {
    setTag(R.id.margin_decorators, marginDecorators)
}