package se.infomaker.iap.theme.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.navigaglobal.mobile.R
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.debug.DebugPainter
import se.infomaker.iap.theme.debug.DebugUtil
import se.infomaker.iap.theme.extensions.setCursorDrawableColor

class ThemeableEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatEditText(context, attrs), Themeable {

    private val debugPainter = DebugPainter(DebugPainter.GREEN, DebugPainter.Position.TOP_RIGHT)
    private val themeKey: String?
    private val hintThemeColorKey: String?

    private var showDebug = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemeableEditText)
        themeKey = typedArray.getString(R.styleable.ThemeableEditText_themeKey)
        hintThemeColorKey = typedArray.getString(R.styleable.ThemeableEditText_hintThemeColor)
        typedArray.recycle()
        debugPainter.setDebugMessage(buildString {
            themeKey?.let {
                append("T: $it")
            }
        })
    }

    override fun apply(theme: Theme) {
        showDebug = ThemeManager.getInstance(context).showDebug()
        if (showDebug) {
            DebugUtil.enableDrawOutside(this)
        }

        theme.getText(themeKey, null)?.let { textStyle ->
            val textColor = textStyle.getColor(theme).get()
            setTextColor(textColor)
            textSize = textStyle.getSize(theme).size
            typeface = textStyle.getFont(theme).typeface
            setCursorDrawableColor(textColor)
        }

        theme.getColor(hintThemeColorKey, null)?.let { hintColor ->
            setHintTextColor(hintColor.get())
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (showDebug) {
            debugPainter.paint(canvas, this)
        }
    }
}