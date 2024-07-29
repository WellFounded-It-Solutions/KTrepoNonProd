package se.infomaker.frt.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.Snackbar
import com.navigaglobal.mobile.R
import se.infomaker.frt.ui.view.extensions.safeRequestLayout
import kotlin.math.min

class BottomAdBannerBehavior @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<FrameLayout>() {

    private var snackbarLayout: Snackbar.SnackbarLayout? = null

    private var mainContentViewId: Int? = null

    private var mainContentView: View? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BottomAdBannerBehavior)
        mainContentViewId = a.getResourceId(R.styleable.BottomAdBannerBehavior_behavior_contentView, 0)
        a.recycle()
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean {
        (dependency as? Snackbar.SnackbarLayout)?.let {
            updateSnackbarDependency(child, it)
            return true
        }
        if (dependency.id == mainContentViewId) {
            updateMainContentViewDependency(child, dependency)
        }
        return false
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: FrameLayout, layoutDirection: Int): Boolean {
        updateMainContentViewPadding(child)
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    private fun updateMainContentViewDependency(child: FrameLayout, dependency: View) {
        mainContentView = dependency
        updateMainContentViewPadding(child)
    }

    private fun updateMainContentViewPadding(child: FrameLayout) {
        val offset = child.height
        if (mainContentView?.paddingBottom != offset) {
            mainContentView?.updatePadding(bottom = offset)
            mainContentView?.safeRequestLayout()
        }
    }

    private fun updateSnackbarDependency(child: FrameLayout, snackbarLayout: Snackbar.SnackbarLayout) {
        this.snackbarLayout = snackbarLayout
        offsetSnackbarBy(child)
    }

    private fun offsetSnackbarBy(child: FrameLayout) {
        val offset = min(0f, child.translationY - child.height.toFloat())
        snackbarLayout?.translationY = offset
        snackbarLayout?.safeRequestLayout()
    }
}