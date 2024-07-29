package se.infomaker.iap.articleview.item.decorator

import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor

data class DecoratorColors(val colorThemeKeys: List<String>) {
    fun getColor(theme: Theme, defaultThemeColor: ThemeColor = ThemeColor.TRANSPARENT): ThemeColor {
        colorThemeKeys.forEach {
            val color = theme.getColor(it, ThemeColor.TRANSPARENT)
            if (color != ThemeColor.TRANSPARENT) {
                return color
            }
        }
        return defaultThemeColor
    }
}