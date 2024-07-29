package se.infomaker.livecontentui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

/**
 * @deprecated This no longer does what it once did.
 *             To not offset views when app bar is offset out of view,
 *             use {@link se.infomaker.livecontentui.view.appbar.NonScrollConsumingAppBarLayoutBehavior}.
 */
@Deprecated
public class AppbBarTransparentScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior{

    public AppbBarTransparentScrollingViewBehavior() {
        super();
    }

    public AppbBarTransparentScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child,
                                          View dependency) {
        updateOffset(parent, child, dependency);
        return false;
    }

    private boolean updateOffset(CoordinatorLayout parent, View child,
                                 View dependency) {
        final CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) dependency
                .getLayoutParams()).getBehavior();
        if (behavior instanceof AppBarLayout.Behavior) {
            // Offset the child so that it is below the app-bar (with any
            // overlap)
            final int offset = 0;   // CHANGED TO 0
            setTopAndBottomOffset(offset);
            return true;
        }
        return false;
    }
}
