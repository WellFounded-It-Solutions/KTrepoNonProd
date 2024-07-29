package se.infomaker.livecontentui.livecontentrecyclerview.manager;

import android.content.Context;
import android.graphics.PointF;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import se.infomaker.iap.theme.util.UI;

public class ScrollingLayoutManager extends GridLayoutManager {
    private final int duration;

    public ScrollingLayoutManager(Context context, int orientation, boolean reverseLayout, int duration) {
        super(context, 1, orientation, reverseLayout);
        this.duration = duration;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        int extraLayoutSpace = super.getExtraLayoutSpace(state);
        if (extraLayoutSpace == 0) {
            return (int) UI.dp2px(300);
        }
        return extraLayoutSpace;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        View firstVisibleChild = recyclerView.getChildAt(0);
        if (firstVisibleChild == null) {
            return;
        }
        int itemHeight = firstVisibleChild.getHeight();
        int currentPosition = recyclerView.getChildAdapterPosition(firstVisibleChild);
        int distanceInPixels = Math.abs((currentPosition - position) * itemHeight);
        if (distanceInPixels == 0) {
            distanceInPixels = (int) Math.abs(firstVisibleChild.getY());
        }
        SmoothScroller smoothScroller = new SmoothScroller(recyclerView.getContext(), distanceInPixels, duration);
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    private class SmoothScroller extends LinearSmoothScroller {
        private static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;
        private final float distanceInPixels;
        private final float duration;

        public SmoothScroller(Context context, int distanceInPixels, int duration) {
            super(context);
            this.distanceInPixels = distanceInPixels;
            float millisecondsPerPx = calculateSpeedPerPixel(context.getResources().getDisplayMetrics());
            this.duration = distanceInPixels < TARGET_SEEK_SCROLL_DISTANCE_PX ?
                    (int) (Math.abs(distanceInPixels) * millisecondsPerPx) : duration;
        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return ScrollingLayoutManager.this
                    .computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int calculateTimeForScrolling(int dx) {
            float proportion = (float) dx / distanceInPixels;
            return (int) (duration * proportion);
        }
    }
}