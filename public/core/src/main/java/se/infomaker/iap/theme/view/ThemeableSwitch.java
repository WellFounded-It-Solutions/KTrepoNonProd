package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.ColorUtils;

import com.navigaglobal.mobile.R;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;

public class ThemeableSwitch extends SwitchCompat implements Themeable {

    private String themeKey;
    private DebugPainter debugPainter = new DebugPainter(DebugPainter.GREEN, DebugPainter.Position.TOP_RIGHT);
    private boolean showDebug = false;

    public ThemeableSwitch(Context context) {
        super(context);
    }

    public ThemeableSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray buttonTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableMaterialButton);
        themeKey = buttonTypedArray .getString(R.styleable.ThemeableMaterialButton_themeKey);
        buttonTypedArray.recycle();
        debugPainter.setDebugMessage("C: " + themeKey);
    }

    @Override
    public void apply(Theme theme) {
        showDebug = ThemeManager.getInstance(getContext()).showDebug();
        if (showDebug) {
            DebugUtil.enableDrawOutside(this);
        }
        if (themeKey != null) {
            ThemeColor color = theme.getColor(themeKey, null);
            if (color != null) {
                int checked = color.get();
                int unchecked = Color.WHITE;
                int disabled = Color.LTGRAY;
                setThumbTintList(getButtonColorStateList(checked, unchecked, disabled));
                setTrackTintList(getButtonColorStateList(
                        ColorUtils.blendARGB(checked, Color.WHITE, 0.7f),
                        Color.LTGRAY,
                        ColorUtils.blendARGB(Color.GRAY, Color.WHITE, 0.8f))
                );
            }
        }
    }

    public static ColorStateList getButtonColorStateList(int checked, int unchecked, int disabled) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled, android.R.attr.state_checked}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[] {
                checked,
                disabled,
                unchecked,
                Color.BLUE
        };

        return new ColorStateList(states, colors);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }
}
