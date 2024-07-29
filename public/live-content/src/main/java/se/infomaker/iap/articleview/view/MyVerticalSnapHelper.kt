package se.infomaker.iap.articleview.view

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.navigaglobal.mobile.ktx.findAncestorOfType

class MyVerticalSnapHelper : androidx.recyclerview.widget.LinearSnapHelper() {
    private var verticalHelper: androidx.recyclerview.widget.OrientationHelper? = null

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if (layoutManager is LinearLayoutManager) {
            return getStartView(layoutManager, getHelper(layoutManager))
        }
        return super.findSnapView(layoutManager)
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = IntArray(2)
        out[0] = 0

        if (layoutManager is LinearLayoutManager && layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, getHelper(layoutManager))

            if (layoutManager.isAtTop) {
                targetView.findAncestorOfType(AppBarLayout::class.java)?.setExpanded(true)
            }

        } else {
            out[1] = 0
        }

        return out
    }

    private fun getStartView(layoutManager: RecyclerView.LayoutManager, helper: androidx.recyclerview.widget.OrientationHelper): View? {
        if (layoutManager is LinearLayoutManager) {
            val firstChild = layoutManager.findFirstVisibleItemPosition()
            val isLastItem = layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1
            if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                return null
            }

            val child = layoutManager.findViewByPosition(firstChild)
            child?.let { view ->
                if (view.tag == "snap") {
                    return if (helper.getDecoratedEnd(view) >= helper.getDecoratedMeasurement(view) / 2 && helper.getDecoratedEnd(view) > 0) {
                        view
                    } else {
                        if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                            null
                        } else {
                            layoutManager.findViewByPosition(firstChild + 1)
                        }
                    }
                }
            }
        }

        return null
    }

    private fun distanceToStart(targetView: View, helper: androidx.recyclerview.widget.OrientationHelper): Int =
            helper.getDecoratedStart(targetView) - helper.startAfterPadding

    private fun getHelper(layoutManager: LinearLayoutManager): androidx.recyclerview.widget.OrientationHelper {
        if (verticalHelper == null) {
            verticalHelper = androidx.recyclerview.widget.OrientationHelper.createVerticalHelper(layoutManager)
        }
        return verticalHelper!!
    }
}

private val LinearLayoutManager.isAtTop: Boolean
    get() = findFirstCompletelyVisibleItemPosition() == 0