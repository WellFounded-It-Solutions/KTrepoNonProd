package se.infomaker.livecontentui.livecontentdetailview.swipe;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

/**
 * Created by lohnn on 26/02/16.
 * © Infomaker Scandinavia AB
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {
    public static final String DEPTH_EFFECT = "depth";
    private static final float MIN_SCALE = .8f;
    private static final float ALPHA_OFFSET = .3f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);
        } else if (position <= 1) { // [0,1]
            // Fade the page out.
            view.setAlpha(1 - position + ALPHA_OFFSET);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}