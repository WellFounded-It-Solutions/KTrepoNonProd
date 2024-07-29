package se.infomaker.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.utilities.toComposeColor
import kotlin.reflect.KProperty

class ThemeColorListDelegate(
    context: Context,
    themeKeys: List<String>? = null,
    default: Color? = null
) {
    val color: Color = run {
        val themeManager = ThemeManager.getInstance(context)
        themeKeys?.firstNotNullOfOrNull { key ->
            themeManager.appTheme.getColor(key, null)
        }?.toComposeColor ?: default ?: Color.Transparent
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Color {
        return color
    }
}


