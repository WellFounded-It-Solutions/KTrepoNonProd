package se.infomaker.iap.theme.ktx

import android.view.View
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.view.ThemeableTouch

fun View.getThemeableTouchColor(theme: Theme): ThemeColor {
    return (this as? ThemeableTouch)?.getTouchColor(theme)
        ?: theme.getColor(ThemeableTouch.DEFAULT_TOUCH_COLOR_KEY, null)
        ?: ThemeableTouch.DEFAULT_TOUCH_COLOR
}