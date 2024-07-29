package se.infomaker.livecontentui.livecontentrecyclerview.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize

class ContentListItemBoundaryDecoration(
    private val spacing: Int,
    private val listItemSeparator: DrawSeparatorAction,
    private val headerSeparator: DrawSeparatorAction,
    private val relatedSeparator: DrawSeparatorAction,
    private val footerSeparator: DrawSeparatorAction
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val childPosition = parent.getChildAdapterPosition(view)
        if (childPosition == 0) {
            // Skip first item
            return
        }

        val viewHolder = parent.getChildViewHolder(view)
        val previousViewHolder = parent.findViewHolderForAdapterPosition(childPosition - 1)
        outRect.top = if (shouldBlockSpacing(viewHolder, previousViewHolder)) 0 else spacing
    }

    private fun shouldBlockSpacing(viewHolder: RecyclerView.ViewHolder, previousViewHolder: RecyclerView.ViewHolder?) =
        previousViewHolder?.holdsHeaderView == true || viewHolder.holdsRelatedView || viewHolder.holdsFooterView

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // Start at 1 to skip first.
        for (i in 1 until parent.childCount) {
            val child = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(child)
            val previousChild = parent.getChildAt(i - 1)
            val previousViewHolder = parent.getChildViewHolder(previousChild)
            if (previousViewHolder.holdsHeaderView) {
                headerSeparator(canvas, child)
            }
            else if (viewHolder.holdsRelatedView) {
                relatedSeparator(canvas, child)
            }
            else if (viewHolder.holdsFooterView) {
                footerSeparator(canvas, child)
            }
            else if (spacing == 0) {
                if (previousViewHolder.allowsSeparator && viewHolder.allowsSeparator) {
                    listItemSeparator(canvas, child)
                }
            }
        }
    }

    companion object Factory {
        private val DEFAULT_SPACING = ThemeSize(6f)

        @JvmStatic
        fun create(theme: Theme): ContentListItemBoundaryDecoration {
            return ContentListItemBoundaryDecoration(
                theme.getSize("contentListItemSpacing", DEFAULT_SPACING).sizePx.toInt(),
                DrawSeparatorAction(theme = theme),
                headerSeparator = DrawSeparatorAction(theme = theme, themeKeyPrefix = "header"),
                relatedSeparator = DrawSeparatorAction(theme = theme, themeKeyPrefix = "related"),
                footerSeparator = DrawSeparatorAction(theme = theme, themeKeyPrefix = "footer")
            )
        }
    }
}