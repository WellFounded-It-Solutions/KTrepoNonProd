package se.infomaker.iap.theme.style.decoration

import android.graphics.Paint
import android.text.TextPaint
import android.widget.TextView

data class ThemeUnderline(private val decoration: ThemeLineDecoration) {

    fun apply(textView: TextView) {
        if (decoration == ThemeLineDecoration.SINGLE) {
            textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    fun apply(paint: TextPaint) {
        if (decoration == ThemeLineDecoration.SINGLE) {
            paint.isUnderlineText = true
        }
    }

    companion object {
        @JvmField val DEFAULT = ThemeUnderline(ThemeLineDecoration.NONE)
    }
}