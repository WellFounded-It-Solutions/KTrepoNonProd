package se.infomaker.iap.theme.letterspacing

import android.text.TextPaint
import android.widget.TextView

data class ThemeLetterSpacing(private val spacing: Float) {

    fun apply(textView: TextView) {
        textView.letterSpacing = spacing
    }

    fun paint(paint: TextPaint) {
        paint.letterSpacing = spacing
    }

    companion object {
        private const val DEFAULT_LETTER_SPACING = .0F
        @JvmField val DEFAULT = ThemeLetterSpacing(DEFAULT_LETTER_SPACING)
    }
}