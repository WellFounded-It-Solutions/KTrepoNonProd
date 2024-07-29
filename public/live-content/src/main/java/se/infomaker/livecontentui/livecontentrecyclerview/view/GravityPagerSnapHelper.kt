package se.infomaker.livecontentui.livecontentrecyclerview.view

import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * This class is heavily inspired by its super class.
 *
 * The difference is that it is snapping to
 *  * [android.view.Gravity.START]
 *  * [android.view.Gravity.CENTER]
 *  * [android.view.Gravity.END]
 *
 * but it only supports horizontal scrolling, since that is what we needed.
 */
class GravityPagerSnapHelper(private val gravity: Int) : PagerSnapHelper() {

    private var horizontalHelper: OrientationHelper? = null

    /**
     * Check super class for details
     */
    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceTo(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }
        return out
    }

    private fun distanceTo(targetView: View, helper: OrientationHelper): Int {
        return when(gravity) {
            Gravity.CENTER -> distanceToCenter(targetView, helper)
            Gravity.END -> distanceToEnd(targetView, helper)
            else -> distanceToStart(targetView, helper)
        }
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        val childStart = helper.getDecoratedStart(targetView)
        val containerStart = helper.startAfterPadding
        return childStart - containerStart
    }

    private fun distanceToCenter(targetView: View, helper: OrientationHelper): Int {
        val childCenter = helper.getDecoratedStart(targetView) +
                (helper.getDecoratedMeasurement(targetView) / 2)
        val containerCenter = helper.startAfterPadding + helper.totalSpace / 2
        return childCenter - containerCenter
    }

    private fun distanceToEnd(targetView: View, helper: OrientationHelper): Int {
        val childEnd = helper.getDecoratedEnd(targetView)
        val containerEnd = helper.endAfterPadding
        return childEnd - containerEnd
    }

    /**
     * Check super class for details
     */
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager.canScrollHorizontally()) {
            return findView(layoutManager, getHorizontalHelper(layoutManager))
        }
        return null
    }

    private fun findView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }

        return when(gravity) {
            Gravity.CENTER -> findCenterView(layoutManager, helper)
            Gravity.END -> findEndView(layoutManager, helper)
            else -> findStartView(layoutManager, helper)
        }
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {

        var closestChild: View? = null
        val start = if (layoutManager.clipToPadding) helper.startAfterPadding else 0

        var absClosest = Int.MAX_VALUE

        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)
            val childStart = helper.getDecoratedStart(child)
            val absDistance = abs(childStart - start)

            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }

        return closestChild
    }

    private fun findCenterView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {

        var closestChild: View? = null
        val start = if (layoutManager.clipToPadding) helper.startAfterPadding else 0
        val center = start + helper.totalSpace / 2

        var absClosest = Int.MAX_VALUE

        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = abs(childCenter - center)

            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }

        return closestChild
    }

    private fun findEndView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {

        var closestChild: View? = null
        val end = if (layoutManager.clipToPadding) helper.endAfterPadding else helper.totalSpace

        var absClosest = Int.MAX_VALUE

        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)
            val childEnd = helper.getDecoratedEnd(child)
            val absDistance = abs(childEnd - end)

            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }

        return closestChild
    }

    /**
     * Check super class for details
     */
    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val helper = getHorizontalHelper(layoutManager)

        var closestChildBefore: View? = null
        var distanceBefore = Int.MAX_VALUE
        var closestChildAfter: View? = null
        var distanceAfter = Int.MAX_VALUE

        val childCount = layoutManager.childCount
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val distance = distanceTo(child, helper)

            if (distance in (distanceBefore + 1)..0) {
                distanceBefore = distance
                closestChildBefore = child
            }
            if (distance in 0 until distanceAfter) {
                distanceAfter = distance
                closestChildAfter = child
            }
        }

        val forwardFling = velocityX > 0
        if (forwardFling && closestChildAfter != null) {
            return layoutManager.getPosition(closestChildAfter)
        } else if (!forwardFling && closestChildBefore != null) {
            return layoutManager.getPosition(closestChildBefore)
        }

        val visibleView = (if (forwardFling) closestChildBefore else closestChildAfter)
            ?: return RecyclerView.NO_POSITION
        val visiblePosition = layoutManager.getPosition(visibleView)
        val snapToPosition = visiblePosition + (if (forwardFling) +1 else -1)

        if (snapToPosition < 0 || snapToPosition >= itemCount) {
            return RecyclerView.NO_POSITION
        }
        return snapToPosition
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        horizontalHelper?.let {
            return it
        }
        return OrientationHelper.createHorizontalHelper(layoutManager).also {
            horizontalHelper = it
        }
    }
}