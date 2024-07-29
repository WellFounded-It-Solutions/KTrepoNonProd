package se.infomaker.iap.theme.alignment

import android.widget.TextView
import com.google.gson.annotations.SerializedName

class ThemeAlignment(private val _alignment: Int) {

    val alignment: Int get() = _alignment

    companion object {
        @JvmField
        val DEFAULT = ThemeAlignment(ThemeAlignments.INHERIT.ordinal)
    }

    fun apply(textView: TextView) = when (_alignment) {
        ThemeAlignments.LEFT.ordinal -> style(textView, TextView.TEXT_ALIGNMENT_VIEW_START)
        ThemeAlignments.RIGHT.ordinal -> style(textView, TextView.TEXT_ALIGNMENT_VIEW_END)
        ThemeAlignments.CENTER.ordinal -> style(textView, TextView.TEXT_ALIGNMENT_CENTER)
        else -> style(textView, TextView.TEXT_ALIGNMENT_INHERIT)
    }

    private fun style(textView: TextView, textAlignment: Int) {
        textView.textAlignment = textAlignment
    }

    override fun toString(): String = ("ThemeAlignment(alignment=\"${alignment}\")")
}

enum class ThemeAlignments {
    @SerializedName("inherit")
    INHERIT,

    @SerializedName("left")
    LEFT,

    @SerializedName("right")
    RIGHT,

    @SerializedName("center")
    CENTER
}

