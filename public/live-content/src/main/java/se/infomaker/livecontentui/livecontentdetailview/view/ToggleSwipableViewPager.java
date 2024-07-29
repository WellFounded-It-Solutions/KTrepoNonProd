package se.infomaker.livecontentui.livecontentdetailview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import timber.log.Timber;

public class ToggleSwipableViewPager extends ViewPager {
    private boolean canSwipe = true;

    public void setCanSwipe(boolean canSwipe) {
        this.canSwipe = canSwipe;
    }

    public ToggleSwipableViewPager(Context context) {
        super(context);
    }

    public ToggleSwipableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return canSwipe && super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            Timber.w(e, "Something went wrong when checking if we should intercept touch");
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return canSwipe && super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            Timber.w(e, "Something went wrong when touching");
            return false;
        }
    }
}
