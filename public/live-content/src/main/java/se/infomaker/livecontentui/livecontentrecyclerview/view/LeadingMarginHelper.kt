package se.infomaker.livecontentui.livecontentrecyclerview.view

import android.view.View
import android.widget.TextView
import kotlin.math.roundToInt

class LeadingMarginHelper(
    val property: String?,
    private val targetId: Int,
    private val padding: Int,
    private val onPropertyValueChanged: () -> Unit
) {
    private val invisiblePrefixBuilder = StringBuilder()
    private var shouldRenderLeadingMargin = property == null

    var leadingMarginPropertyValue: String? = null
        set(value) {
            shouldRenderLeadingMargin = value.toBoolean()
            if (value != field) {
                onPropertyValueChanged.invoke()
            }
            field = value
        }

    fun shouldConsiderLeadingMargin() = targetId != 0 && shouldRenderLeadingMargin

    fun buildPrefix(textView: TextView): String {
        (textView.parent as? View)?.findViewById<View>(targetId)?.let { target ->
            val measuredLeadingMargin = target.measuredWidth
            val leadingMargin = if (measuredLeadingMargin > 0) measuredLeadingMargin + padding else 0
            val singleWhitespaceWidth = textView.paint.measureText(WHITESPACE)
            invisiblePrefixBuilder.setLength(0)
            for (i in 0 until (leadingMargin/singleWhitespaceWidth).roundToInt()) {
                invisiblePrefixBuilder.append(WHITESPACE)
            }
            return invisiblePrefixBuilder.toString()
        }
        return ""
    }

    companion object {
        private const val WHITESPACE = " "
    }
}