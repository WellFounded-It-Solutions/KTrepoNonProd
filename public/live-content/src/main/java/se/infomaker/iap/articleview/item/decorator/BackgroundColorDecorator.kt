package se.infomaker.iap.articleview.item.decorator

import android.view.View
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor

data class BackgroundColorDecorator(val backgroundColors: DecoratorColors) : ItemDecorator {
    override fun decorate(item: Item, itemView: View, theme: Theme) {
        val color = backgroundColors.getColor(theme)
        if (color != ThemeColor.TRANSPARENT) {
            itemView.setBackgroundColor(color.get())
        }
    }
}
