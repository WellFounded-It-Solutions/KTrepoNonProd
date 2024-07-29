package se.infomaker.livecontentui.view.appbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

/**
 * This subclass of the default [AppBarLayout.Behavior] attached to an [AppBarLayout] does
 * the exact same this with one important difference:
 *
 *     IT DOES NOT CONSUME THE SCROLL DISTANCE IT INTERCEPTS
 *
 * This is to allow it to be used when the app bar is translucent to render the content underneath
 * and don't offset views on initial scroll until the app bar is completely hidden.
 */
class NonScrollConsumingAppBarLayoutBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null) : AppBarLayout.Behavior(context, attrs) {

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        consumed[1] = 0
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        consumed[1] = 0
    }
}