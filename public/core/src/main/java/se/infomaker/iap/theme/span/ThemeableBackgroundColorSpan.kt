package se.infomaker.iap.theme.span

import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.view.Themeable

class ThemeableBackgroundColorSpan(
    private val backgroundColorKeys: List<String>,
    private val fallbackBackgroundColor: ThemeColor
) : CharacterStyle(), UpdateAppearance, Themeable {

    private var theme: Theme? = null

    override fun updateDrawState(textPaint: TextPaint) {
        val backgroundColor = theme?.getColor(backgroundColorKeys, null) ?: fallbackBackgroundColor
        textPaint.bgColor = backgroundColor.get()
    }

    override fun apply(theme: Theme) {
        this.theme = theme
    }
}