package se.infomaker.iap.theme.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.navigaglobal.mobile.R
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.debug.DebugPainter
import se.infomaker.iap.theme.debug.DebugUtil
import se.infomaker.iap.theme.util.UI

open class ThemeableCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr), Themeable, ThemeableTouch {

    private val debugPainter = DebugPainter(DebugPainter.BLUE, DebugPainter.Position.BOTTOM_LEFT)

    override val touchColorKey by lazy { themeTouchColor ?: ThemeableTouch.DEFAULT_TOUCH_COLOR_KEY }

    private var themeTouchColor: String? = null
    private var themeBackgroundColor: String? = null
    private var themeFallbackBackgroundColor: String? = null
    private var showDebug = false

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ThemeableCardView)
            themeTouchColor = typedArray.getString(R.styleable.ThemeableCardView_themeTouchColor)
            themeBackgroundColor = typedArray.getString(R.styleable.ThemeableCardView_themeBackgroundColor)
            themeFallbackBackgroundColor = typedArray.getString(R.styleable.ThemeableCardView_themeFallbackBackgroundColor)
            typedArray.recycle()
        }

        val builder = StringBuilder()
        themeBackgroundColor?.let {
            builder.append("BC: ").append(it)
        }
        themeFallbackBackgroundColor?.let {
            builder.append(" FBC: ").append(it)
        }
        builder.append(" TC: ").append(touchColorKey)
        debugPainter.setDebugMessage(builder.toString())
    }

    override fun apply(theme: Theme) {
        showDebug = ThemeManager.getInstance(context).showDebug()
        if (showDebug) {
            DebugUtil.enableDrawOutside(this)
        }
        val ripple = UI.getAdaptiveRippleDrawable(Color.TRANSPARENT, getTouchColor(theme).get())
        foreground = ripple

        if (themeBackgroundColor?.isNotEmpty() == true || themeFallbackBackgroundColor?.isNotEmpty() == true) {
            setCardBackgroundColor(getBackgroundColor(theme).get())
        }
    }

    private fun getBackgroundColor(theme: Theme): ThemeColor {
        val fallback = ThemeableUtil.getThemeColor(theme, themeFallbackBackgroundColor, ThemeColor.TRANSPARENT)
        return ThemeableUtil.getThemeColor(theme, themeBackgroundColor, fallback)
    }

    override fun getTouchColor(theme: Theme): ThemeColor {
        return ThemeableUtil.getThemeColor(theme, touchColorKey, null) ?: ThemeableTouch.DEFAULT_TOUCH_COLOR
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (showDebug) {
            debugPainter.paint(canvas, this)
        }
    }
}