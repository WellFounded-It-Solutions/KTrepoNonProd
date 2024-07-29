package se.infomaker.iap.theme.util;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;


/**
 * The same old helpers as everywhere else ;)
 */
public class UI {
    public static float dp2px(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float px2dp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void setTouchFeedback(View view, int color) {
        view.setBackground(getAdaptiveRippleDrawable(Color.TRANSPARENT, color));
    }

    public static void setTouchFeedback(View view, int normal, int pressed) {
        view.setBackground(getAdaptiveRippleDrawable(normal, pressed));
    }

    public static void setTouchFeedback(View view, int normal, int selected, int pressed) {
        view.setBackground(getAdaptiveRippleDrawable(normal, selected, pressed));
    }


    public static void setForegroundTouchFeedback(View view, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setForeground(getAdaptiveRippleDrawable(Color.TRANSPARENT, color));
        }
    }

    public static Drawable getAdaptiveRippleDrawable(int normalColor, int pressedColor) {
        return getAdaptiveRippleDrawable(normalColor, normalColor, pressedColor);
    }

    public static Drawable getAdaptiveRippleDrawable(int normalColor, int selectedColor, int pressedColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int[][] states = new int[][] {
                    new int[] {}  // normal
            };
            int[] colors = new int[] {
                    pressedColor
            };

            ColorStateList colorStateList = new ColorStateList(states, colors);
            StateListDrawable contentDrawable = new StateListDrawable();
            contentDrawable.addState(new int[]{}, new ColorDrawable(normalColor));
            if (selectedColor != normalColor && selectedColor != Color.TRANSPARENT) {
                contentDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(selectedColor));
            }
            return new RippleDrawable(colorStateList, contentDrawable, new ColorDrawable(Color.BLACK));
        } else {
            return getStateListDrawable(normalColor, selectedColor, pressedColor);
        }
    }

    public static StateListDrawable getStateListDrawable(int normalColor, int selectedColor, int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressedColor));
        states.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(selectedColor));
        states.addState(new int[]{android.R.attr.state_focused}, new ColorDrawable(pressedColor));
        states.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(pressedColor));
        states.addState(new int[]{}, new ColorDrawable(normalColor));
        return states;
    }
}
