package se.infomaker.iap.articleview.preprocessor.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Layout
import android.text.SpanWatcher
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan
import android.text.style.UpdateLayout
import androidx.core.text.getSpans
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.Themeable
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

/**
 * A custom [LeadingMarginSpan.LeadingMarginSpan2] implementation that renders a [dropCap]
 * inside of a leading span that spans up to 3 lines of a text.
 *
 * Has to implement [UpdateLayout] in order to propagate changes to [SpanWatcher]s.
 */
class DropCapSpan(
    private val dropCap: String,
    private val originalDropCapSpans: List<CharacterStyle>,
    private val themeKey: String?
) : LeadingMarginSpan.LeadingMarginSpan2, UpdateLayout, Themeable {

    // Custom theme variables
    private var dropCapTypeface: Typeface? = null
    private var dropCapTextColor: Int? = null

    // Internal calculations
    private var lineCount = 0
    private var dropCapTextSize = 0f
    private var dropCapWidth = 0

    private val dropCapRect = Rect()

    override fun getLeadingMargin(first: Boolean): Int {
        return if (first && dropCapWidth > 0) {
            if (leadingMarginLineCount == 0) {
                // No leading margin, just the width of the drop cap
                dropCapWidth
            }
            else {
                dropCapWidth + DROP_CAP_MARGIN
            }
        }
        else {
            0
        }
    }

    override fun drawLeadingMargin(canvas: Canvas, paint: Paint, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence, start: Int, end: Int, first: Boolean, layout: Layout) {
        if (first) {
            val previousLineCount = lineCount
            lineCount = layout.lineCount

            if (leadingMarginLineCount == 0) {
                // No lines with leading margin, just render the drop cap normally.
                (paint as? TextPaint)?.let { textPaint ->
                    originalDropCapSpans.forEach { styleSpan ->
                        styleSpan.updateDrawState(textPaint)
                        if (styleSpan is MetricAffectingSpan) {
                            styleSpan.updateMeasureState(textPaint)
                        }
                    }
                }
                dropCapWidth = paint.measureText(dropCap).toInt()
                canvas.drawText(dropCap, 0f, baseline.toFloat(), paint)
                return
            }

            // Create a new text paint to use when drawing the drop cap. Re-using and re-setting values on
            // the original paint is not a good idea for older versions of Android. (And it probably is
            // not a good idea at all anyways, tbh..)
            val dropCapPaint = TextPaint(paint)
            dropCapTypeface?.let { dropCapPaint.typeface = it }
            dropCapTextColor?.let { dropCapPaint.color = it }

            var dropCapBaseline = layout.getLineBaseline(max(0, leadingMarginLineCount - 1)).toFloat()

            // If we haven't measured before or if the line count changed after we first added
            // a leading margin, calculate drop cap text size and width and notify
            if ((dropCapWidth == 0 && dropCapTextSize == 0f) || previousLineCount != lineCount) {

                val lineHeight = paint.fontMetrics.bottom - paint.fontMetrics.top
                val lineHeightWithSpacing = lineHeight * layout.spacingMultiplier + layout.spacingAdd
                val lineSpacing = lineHeightWithSpacing - lineHeight
                val textSize = (lineHeightWithSpacing * leadingMarginLineCount - (lineSpacing))

                // Top inset, should we measure text here..?
                // How do we know the actual top inset from a Rect?
                // TODO: Find a better way to do this.
                val topInset = baseline + paint.fontMetrics.ascent + -(layout.topPadding)

                // This is the maximum height of the drop cap
                val allowedDropCapHeight = dropCapBaseline - topInset
                dropCapTextSize = calculateDropCapTextSize(dropCapPaint, dropCap, textSize, allowedDropCapHeight)

                dropCapPaint.textSize = dropCapTextSize
                dropCapWidth = dropCapPaint.measureText(dropCap).toInt()

                // Since we have changed the actual values that will be used to determine
                // both the number of lines that will have the margin indent and the actual margin
                // itself, we need to notify SpanWatcher that are currently attached to the
                // SpannableString we are manipulating that things have changed to make the text
                // re-render correctly and not be cut off.
                (text as? SpannableString)?.getSpans<SpanWatcher>()?.forEach { spanWatcher ->
                    spanWatcher.onSpanChanged(text, this, 0, text.length, 0, text.length)
                }
            }

            dropCapPaint.textSize = dropCapTextSize

            // Some dropCaps in some fonts render below the baseline, which means that
            // even though our calculations for text size are correct the drop cap is
            // rendered a few pixels "below" the baseline.
            // We measure the text one more time to get the amount of pixels the drop cap is
            // rendered below the base and subtract it from the baseline to offset the drop cap
            // correctly.
            dropCapPaint.getTextBounds(dropCap, 0, 1, dropCapRect)
            dropCapBaseline -= dropCapRect.bottom

            canvas.drawText(dropCap, 0f, dropCapBaseline, dropCapPaint)
        }
    }

    private fun calculateDropCapTextSize(
        paint: Paint,
        text: String,
        desiredTextSize: Float,
        allowedHeight: Float,
        rect: Rect = Rect()
    ): Float {
        paint.textSize = desiredTextSize
        paint.getTextBounds(text, 0, 1, rect)
        val measuredHeight = rect.height()
        val maxAllowedHeight = allowedHeight + ALLOWED_MARGIN_OF_ERROR
        val minAllowedHeight = allowedHeight - ALLOWED_MARGIN_OF_ERROR
        Timber.d("Measured drop cap height: $measuredHeight, should be at least $minAllowedHeight and at most $maxAllowedHeight")
        if (measuredHeight > maxAllowedHeight) {
            // Too big
            return calculateDropCapTextSize(paint, text, desiredTextSize - ALLOWED_MARGIN_OF_ERROR, allowedHeight, rect)
        }
        else if (measuredHeight < minAllowedHeight) {
            // Too small
            return calculateDropCapTextSize(paint, text, desiredTextSize + ALLOWED_MARGIN_OF_ERROR, allowedHeight, rect)
        }
        return desiredTextSize
    }

    override fun getLeadingMarginLineCount(): Int {
        if (lineCount < MINIMUM_REQUIRED_LINE_COUNT) return 0
        return max(MINIMUM_REQUIRED_LINE_COUNT, min(DROP_CAP_LINE_COUNT, lineCount))
    }

    override fun apply(theme: Theme) {
        theme.getText(themeKey, null)?.let { themeTextStyle ->
            themeTextStyle.getFont(theme).typeface?.let { dropCapTypeface ->
                this.dropCapTypeface = dropCapTypeface
            }
            this.dropCapTextColor = themeTextStyle.getColor(theme)?.get()
        }
    }

    companion object {
        private const val MINIMUM_REQUIRED_LINE_COUNT = 2
        private const val DROP_CAP_LINE_COUNT = 3
        private val DROP_CAP_MARGIN = 8.dp2px()
        private val ALLOWED_MARGIN_OF_ERROR = 2.dp2px()
    }
}