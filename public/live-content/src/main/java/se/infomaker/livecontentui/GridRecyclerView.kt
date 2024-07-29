package se.infomaker.livecontentui

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.iap.theme.view.ThemeableRecyclerView
import se.infomaker.livecontentui.config.GridLayoutConfig
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.LiveContentRecyclerViewAdapter
import se.infomaker.livecontentui.livecontentrecyclerview.manager.ScrollingLayoutManager
import kotlin.math.max

class GridRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ThemeableRecyclerView(context, attrs) {
    private val itemDecoration = SpacingItemDecoration()

    private var _gridLayoutConfig: GridLayoutConfig = GridLayoutConfig(DEFAULT_WIDTH_LIST, DEFAULT_SPACING_LIST) // Single column grid by default
    private val gridLayoutConfig: GridLayoutConfig get() = _gridLayoutConfig

    companion object{
        const val DEFAULT_WIDTH_GRID = 150
        const val DEFAULT_SPACING_GRID = 4
        const val DEFAULT_WIDTH_LIST = -1
        const val DEFAULT_SPACING_LIST = 0
    }

    /**
     * Sets the [GridLayoutConfig] for the [RecyclerView] decoration. Null will clear config and
     * use default values for the Grid
     * @param config
     */
    fun setGridLayoutConfig(config: GridLayoutConfig?) {
        _gridLayoutConfig = config?.let {
            when {
                it.spacing == 0 && it.width == 0 -> {
                    GridLayoutConfig(DEFAULT_WIDTH_GRID, DEFAULT_SPACING_GRID)
                }
                else -> it
            }
        } ?: GridLayoutConfig(DEFAULT_WIDTH_LIST, DEFAULT_SPACING_LIST)

        itemDecoration.spacing = gridLayoutConfig.spacing.dpToPx()
        requestLayout()
    }

    init {
        layoutManager = ScrollingLayoutManager(context,
                androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
                false,
                1000)
        addItemDecoration(itemDecoration)
    }

    override fun getLayoutManager(): ScrollingLayoutManager? {
        return (super.getLayoutManager() as? ScrollingLayoutManager)?.also {
            it.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    //No more articles/spinner view should span full width of RecyclerView
                    return if (adapter?.getItemViewType(position) == LiveContentRecyclerViewAdapter.VIEW_TYPE_NO_MORE_ARTICLES
                            || adapter?.getItemViewType(position) == LiveContentRecyclerViewAdapter.VIEW_TYPE_LOADING_SPINNER) {
                        layoutManager?.spanCount ?: 1
                    } else {
                        1
                    }
                }
            }
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        layoutManager?.spanCount = if (gridLayoutConfig.width > 0) calculateSpanCount(gridLayoutConfig.width.dpToPx() + gridLayoutConfig.spacing.dpToPx()) else 1
        itemDecoration.spans = layoutManager?.spanCount ?: 1

        super.onMeasure(widthSpec, heightSpec)
    }

    private fun calculateSpanCount(desiredWidth: Int): Int = max(1, measuredWidth / desiredWidth)
}

class SpacingItemDecoration(var spacing: Int = 0) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
    private val halfSpacing
        get() = spacing / 2
    var spans: Int = 1

    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        outRect.apply {
            top = halfSpacing
            bottom = halfSpacing

            val spacings = spans - 1
            val unitSpacing = (spacings.toFloat() * spacing / spans).toInt()

            when (parent.getChildLayoutPosition(view) % spans) {
                0 -> {
                    left = 0
                    right = unitSpacing
                }
                spans - 1 -> {
                    left = unitSpacing
                    right = 0
                }
                else -> {
                    left = unitSpacing / 2
                    right = unitSpacing / 2
                }
            }
        }
    }
}

/**
 * Converts dp value to pixels
 */
private fun Int.dpToPx(): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = this * (metrics.densityDpi / 160f)
    return Math.round(px)
}