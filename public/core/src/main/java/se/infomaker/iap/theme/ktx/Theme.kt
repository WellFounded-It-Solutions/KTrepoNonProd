@file:JvmName("ThemeUtils")

package se.infomaker.iap.theme.ktx

import android.app.Activity
import android.view.Window
import androidx.core.graphics.ColorUtils
import se.infomaker.frtutilities.ktx.applyStatusBarColor
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.attribute.AttributeParseException
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.color.ThemeColorParser

val Theme.backgroundColor: ThemeColor
    get() = getColor(ThemeColor.DEFAULT_BACKGROUND_COLOR, "appBackground", "background")

val Theme.listBackgroundColor: ThemeColor
    get() = getColor(ThemeColor.DEFAULT_LIST_BACKGROUND_COLOR, "listBackground", "background")

val Theme.statusBarColor: ThemeColor
    get() = getColor(chromeColor, "statusbarColor")

val Theme.chromeColor: ThemeColor
    get() = getColor("chromeColor", ThemeColor.DEFAULT_CHROME_COLOR)

val Theme.onChromeColor: ThemeColor
    get() = getColor("onChromeColor", ThemeColor.DEFAULT_ON_CHROME_COLOR)

val Theme.brandColor: ThemeColor
    // Using primaryColor as "fallback" to brandColor for backwards compatibility
    get() = getColor(ThemeColor.DEFAULT_BRAND_COLOR, "brandColor", "primaryColor")

val Theme.onBrandColor: ThemeColor
    get() = getColor("onBrandColor", ThemeColor.DEFAULT_ON_BRAND_COLOR)

val Theme.brandVariantColor: ThemeColor
    get() = getColor(ThemeColor.DEFAULT_BRAND_COLOR, "brandVariantColor", "brandColor")

val Theme.onBrandVariantColor: ThemeColor
    get() = getColor(ThemeColor.DEFAULT_ON_BRAND_COLOR, "onBrandVariantColor", "onBrandColor")

val Theme.textColor: ThemeColor
    get() = getColor("text", ThemeColor.DEFAULT_TEXT_COLOR)

val Theme.toolbarActionColor: ThemeColor
    get() = getColor(ThemeColor.DEFAULT_ON_CHROME_COLOR, "toolbarAction", "onChromeColor")

fun Theme.apply(activity: Activity) {
    apply(activity.findViewById(android.R.id.content))
    apply(activity.window)
}

fun Theme.apply(window: Window) {
    applyToStatusBar(window)
}

private fun Theme.applyToStatusBar(window: Window) {
    window.applyStatusBarColor(statusBarColor.get())
}

fun Int.isBrightColor() = !isDarkColor()

fun Int.isDarkColor() = ColorUtils.calculateLuminance(this) < 0.5

internal fun Int.darkenColor(): Int {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this, hsl)
    hsl[2] *= 0.9f
    return ColorUtils.HSLToColor(hsl)
}

internal fun Theme.parseOrGetColor(color: String?): ThemeColor? {
    return try {
        ThemeColorParser.SHARED_INSTANCE.parseObject(color)
    }
    catch (e: AttributeParseException) {
        getColor(color, null)
    }
}

internal fun Theme.parseOrGetColor(color: String?, fallback: ThemeColor) =
    parseOrGetColor(color) ?: fallback