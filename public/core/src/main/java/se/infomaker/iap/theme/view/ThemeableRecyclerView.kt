package se.infomaker.iap.theme.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.navigaglobal.mobile.R
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.debug.DebugPainter
import se.infomaker.iap.theme.debug.DebugUtil
import se.infomaker.iap.theme.ktx.parseOrGetColor
import se.infomaker.iap.theme.size.ThemeSize

open class ThemeableRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs), Themeable {

    private val dividerDecoration by lazy {
        ThemeDividerDecoration(layoutManager).also {
            addItemDecoration(it)
        }
    }
    private val themeSpacing: String?
    private val themeBackgroundColor: String?
    private val themeFallbackBackgroundColor: String?

    private val debugPainter = DebugPainter(DebugPainter.BLUE, DebugPainter.Position.BOTTOM_LEFT)
    private var showDebug = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemeableRecyclerView)
        themeSpacing = typedArray.getString(R.styleable.ThemeableRecyclerView_themeSpacing)
        themeBackgroundColor = typedArray.getString(R.styleable.ThemeableRecyclerView_themeBackgroundColor)
        themeFallbackBackgroundColor = typedArray.getString(R.styleable.ThemeableRecyclerView_themeFallbackBackgroundColor)
        typedArray.recycle()
        debugPainter.setDebugMessage(buildString {
            themeSpacing?.let {
                append("S: $themeSpacing")
            }
            themeBackgroundColor?.let {
                append(" BC: $themeBackgroundColor")
            }
            themeFallbackBackgroundColor?.let {
                append(" FBC: $themeFallbackBackgroundColor")
            }
        })
    }

    override fun apply(theme: Theme) {
        showDebug = ThemeManager.getInstance(context).showDebug()
        if (showDebug) {
            DebugUtil.enableDrawOutside(this)
        }
        theme.getSize(themeSpacing, null)?.let {
            dividerDecoration.apply {
                size = it
            }
        }
        if (!themeBackgroundColor.isNullOrEmpty() || !themeFallbackBackgroundColor.isNullOrEmpty()) {
            setBackgroundColor(resolveBackgroundColor(theme).get())
        }
    }

    private fun resolveBackgroundColor(theme: Theme): ThemeColor {
        theme.parseOrGetColor(themeBackgroundColor)?.let {
            return it
        }
        return theme.parseOrGetColor(themeFallbackBackgroundColor, ThemeColor.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (showDebug) {
            debugPainter.paint(canvas, this)
        }
    }
}

class ThemeDividerDecoration(private val layoutManager: RecyclerView.LayoutManager?) : RecyclerView.ItemDecoration() {

    lateinit var size: ThemeSize

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        if (parent.getChildAdapterPosition(view) == parent.lastAdapterPosition) {
            return
        }

        (layoutManager as? LinearLayoutManager)?.let {
            if (it.orientation == LinearLayoutManager.VERTICAL) {
                outRect.set(0, 0, 0, size.sizePx.toInt())
            } else {
                outRect.set(0, 0, size.sizePx.toInt(), 0)
            }
        }
    }
}

private val RecyclerView.lastAdapterPosition: Int
    get() = (adapter?.itemCount?.minus(1)) ?: RecyclerView.NO_POSITION