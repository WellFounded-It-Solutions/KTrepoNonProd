package se.infomaker.streamviewer.editpage

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentui.livecontentrecyclerview.decoration.DrawSeparatorAction

class EditPageDividerDecoration(theme: Theme) : RecyclerView.ItemDecoration() {

    private val drawItemSeparator = DrawSeparatorAction(theme, themeKeyPrefix = "topic")

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 1 until parent.childCount) {
            val child = parent.getChildAt(i)
            drawItemSeparator.invoke(canvas, child)
        }
    }
}
