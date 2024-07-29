package se.infomaker.livecontentui.livecontentrecyclerview.decoration

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor

object ContentListDividerFactory {

    private val DEFAULT_DIVIDER_COLOR = ThemeColor(Color.parseColor("#26000000"))

    @JvmStatic
    val size = (1.dp2px() * (3.0 / 4)).toInt()

    @JvmStatic
    @JvmOverloads
    fun create(theme: Theme, themeKeyPrefix: String? = null): Drawable {
        val themeKeys = themeKeyPrefix?.let { listOf("${it}SeparatorColor", "separatorColor") } ?: listOf("separatorColor")
        val dividerColor = theme.getColor(themeKeys, DEFAULT_DIVIDER_COLOR)
        return ColorDrawable(dividerColor.get()).apply { setBounds(0, 0, size, size) }
    }
}