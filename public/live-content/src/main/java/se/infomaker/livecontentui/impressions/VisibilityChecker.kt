package se.infomaker.livecontentui.impressions

import android.graphics.Rect
import android.view.View
import androidx.annotation.Nullable

/**
 * Determine if views are visible to at least a percent
 */
class VisibilityChecker(private val minPercentageViewed: Int) {
    private val mClipRect = Rect()

    /**
     * @return true if the view is visible
     */
    fun isVisible(@Nullable view: View): Boolean {
        if (view.visibility != View.VISIBLE || view.parent == null) {
            return false
        }

        if (!view.getGlobalVisibleRect(mClipRect)) {
            return false
        }

        val visibleArea = mClipRect.height().toLong() * mClipRect.width()
        val totalViewArea = view.height.toLong() * view.width

        return totalViewArea > 0 && 100 * visibleArea >= minPercentageViewed * totalViewArea

    }
}