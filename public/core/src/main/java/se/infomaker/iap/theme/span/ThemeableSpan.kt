package se.infomaker.iap.theme.span

import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.util.DisplayMetrics
import se.infomaker.iap.theme.Theme

class ThemeableSpan(private val theme: Theme, private val themeKeys: List<String>) : MetricAffectingSpan() {
    override fun updateDrawState(paint: TextPaint) {
        applyTheme(paint)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyTheme(paint)
    }

    fun applyTheme(paint: TextPaint) {
        theme.getText(themeKeys, null)?.paint(theme, paint)
    }
}