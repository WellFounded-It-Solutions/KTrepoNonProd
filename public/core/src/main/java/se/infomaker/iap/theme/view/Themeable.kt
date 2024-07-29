package se.infomaker.iap.theme.view

import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor

interface Themeable {
    fun apply(theme: Theme)
}

interface ThemeableTouch {
    val touchColorKey: String

    fun getTouchColor(theme: Theme): ThemeColor

    companion object {
        @JvmField val DEFAULT_TOUCH_COLOR_KEY = "touchfeedback"
        @JvmField val DEFAULT_TOUCH_COLOR = ThemeColor(0x3D000000)
    }
}