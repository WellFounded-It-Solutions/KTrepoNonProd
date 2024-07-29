package se.infomaker.iap.theme.span

import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.Themeable

class ThemeableStyleSpan(
    private val themeKeys: List<String>,
    private val fallback: StyleSpan
) : MetricAffectingSpan(), Themeable {

    private var theme: Theme? = null

    override fun updateDrawState(textPaint: TextPaint) {
        applyTextStyle(textPaint) {
            fallback.updateDrawState(textPaint)
        }
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        applyTextStyle(textPaint) {
            fallback.updateMeasureState(textPaint)
        }
    }

    override fun apply(theme: Theme) {
        this.theme = theme
    }

    private fun applyTextStyle(textPaint: TextPaint, fallback: () -> Unit) {
        theme?.getText(themeKeys, null)?.let {
            it.paint(theme, textPaint)
            return
        }
        fallback()
    }
}