package se.infomaker.frt.ui.view.extensions

import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView

internal fun BottomNavigationView.slideUp() {
    ((layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? HideBottomViewOnScrollBehavior)?.slideUp(this)
}