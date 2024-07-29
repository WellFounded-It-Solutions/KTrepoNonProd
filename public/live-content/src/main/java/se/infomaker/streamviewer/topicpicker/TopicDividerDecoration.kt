package se.infomaker.streamviewer.topicpicker

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentui.livecontentrecyclerview.decoration.DrawSeparatorAction

class TopicDividerDecoration(theme: Theme) : RecyclerView.ItemDecoration() {

    private val drawItemSeparator = DrawSeparatorAction(theme, themeKeyPrefix = "topic")

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 1 until parent.childCount) {
            val child = parent.getChildAt(i)
            val previousChild = parent.getChildAt(i - 1)
            if ((parent.getChildViewHolder(child) as? TopicViewHolder)?.drawDivider == true && (parent.getChildViewHolder(previousChild) as? TopicViewHolder)?.drawDivider == true) {
                drawItemSeparator.invoke(canvas, child)
            }
        }
    }
}