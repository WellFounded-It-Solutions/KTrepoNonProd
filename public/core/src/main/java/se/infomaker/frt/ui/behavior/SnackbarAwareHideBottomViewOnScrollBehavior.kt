package se.infomaker.frt.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.navigaglobal.mobile.R
import se.infomaker.frt.ui.view.extensions.safeRequestLayout
import kotlin.math.min

class SnackbarAwareHideBottomViewOnScrollBehavior(context: Context, attrs: AttributeSet?) : HideBottomViewOnScrollBehavior<BottomNavigationView>(context, attrs) {

    private var snackbarLayout: Snackbar.SnackbarLayout? = null
    private var snackbarAnimator: ViewPropertyAnimator? = null

    private var stickyBottomAdBannerLayout: FrameLayout? = null
    private var stickyBottomAdBannerAnimator: ViewPropertyAnimator? = null

    private val stickyBottomAdBannerId = R.id.bottom_sticky_ad_wrapper

    override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomNavigationView, dependency: View): Boolean {
        (dependency as? Snackbar.SnackbarLayout)?.let {
            updateSnackbarDependency(child, it)
            return true
        }
        (dependency as? FrameLayout)?.let {
            val isBottomAdBanner = it.id == stickyBottomAdBannerId
            if (isBottomAdBanner) {
                updateBottomAdBannerDependency(child, it)
                return true
            }
        }
        return false
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: BottomNavigationView, dependency: View) {
        if (dependency.id == stickyBottomAdBannerId) {
            stickyBottomAdBannerLayout = null
            stickyBottomAdBannerAnimator?.setUpdateListener(null)
        }
        else {
            snackbarLayout = null
            snackbarAnimator?.setUpdateListener(null)
        }
    }

    private fun updateSnackbarDependency(bottomNavigationView: BottomNavigationView, snackbarLayout: Snackbar.SnackbarLayout) {
        this.snackbarLayout = snackbarLayout
        offsetSnackbarBy(bottomNavigationView)
    }

    private fun offsetSnackbarBy(bottomNavigationView: BottomNavigationView) {
        var offset = min(0f, bottomNavigationView.translationY - bottomNavigationView.height.toFloat())
        stickyBottomAdBannerLayout?.let {
            offset = min(0f, it.translationY - it.height.toFloat())
        }

        snackbarLayout?.translationY = offset
        snackbarLayout?.safeRequestLayout()
    }

    private fun updateBottomAdBannerDependency(bottomNavigationView: BottomNavigationView, stickyBottomAdBannerLayout: FrameLayout) {
        this.stickyBottomAdBannerLayout = stickyBottomAdBannerLayout
        offsetBottomAdBannerBy(bottomNavigationView)
    }

    private fun offsetBottomAdBannerBy(bottomNavigationView: BottomNavigationView) {
        val offset = min(0f, bottomNavigationView.translationY - bottomNavigationView.height.toFloat())

        stickyBottomAdBannerLayout?.translationY = offset
        stickyBottomAdBannerLayout?.safeRequestLayout()
    }

    override fun slideDown(child: BottomNavigationView) {
        resetAnimators()
        super.slideDown(child)
        if (snackbarLayout != null) {
            snackbarAnimator = getCurrentAnimator()
            snackbarAnimator?.setUpdateListener {
                offsetSnackbarBy(child)
            }
        }
        if (stickyBottomAdBannerLayout != null) {
            stickyBottomAdBannerAnimator = getCurrentAnimator()
            stickyBottomAdBannerAnimator?.setUpdateListener {
                offsetBottomAdBannerBy(child)
            }
        }
    }

    private fun resetAnimators() {
        snackbarAnimator?.setUpdateListener(null)
        stickyBottomAdBannerAnimator?.setUpdateListener(null)
    }

    override fun slideUp(child: BottomNavigationView) {
        resetAnimators()
        super.slideUp(child)
        if (snackbarLayout != null) {
            snackbarAnimator = getCurrentAnimator()
            snackbarAnimator?.setUpdateListener {
                offsetSnackbarBy(child)
            }
        }
        if (stickyBottomAdBannerLayout != null) {
            stickyBottomAdBannerAnimator = getCurrentAnimator()
            stickyBottomAdBannerAnimator?.setUpdateListener {
                offsetBottomAdBannerBy(child)
            }
        }
    }

    private fun getCurrentAnimator(): ViewPropertyAnimator? {
        return javaClass.superclass.getDeclaredField("currentAnimator").let {
            it.isAccessible = true
            return it.get(this) as? ViewPropertyAnimator
        }
    }
}