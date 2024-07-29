package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.navigaglobal.mobile.R;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.util.UI;

public class ThemeableLinearLayout extends LinearLayoutCompat implements Themeable{
    private String themeBackgroundColor;
    private String themeFallbackBackgroundColor;
    private String themeTouchColor;
    private boolean showDebug = false;

    private DebugPainter debugPainter = new DebugPainter(DebugPainter.BLUE, DebugPainter.Position.BOTTOM_LEFT);

    public void setThemeTouchColor(String themeTouchColor) {
        this.themeTouchColor = themeTouchColor;
    }

    public ThemeableLinearLayout(Context context) {
        super(context);
    }

    public ThemeableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableLinearLayout);
        themeTouchColor = typedArray.getString(R.styleable.ThemeableLinearLayout_themeTouchColor);
        themeBackgroundColor = typedArray.getString(R.styleable.ThemeableLinearLayout_themeBackgroundColor);
        themeFallbackBackgroundColor = typedArray.getString(R.styleable.ThemeableLinearLayout_themeFallbackBackgroundColor);
        typedArray.recycle();

        StringBuilder builder = new StringBuilder();
        if (themeBackgroundColor != null) {
            builder.append("BC: ").append(themeBackgroundColor);
        }
        if (themeFallbackBackgroundColor != null) {
            builder.append(" FBC: ").append(themeFallbackBackgroundColor);
        }
        if (themeTouchColor != null) {
            builder.append(" TC: ").append(themeTouchColor);
        }
        debugPainter.setDebugMessage(builder.toString());
    }

    @Override
    public void apply(@NonNull Theme theme) {
        showDebug = ThemeManager.getInstance(getContext()).showDebug();
        if (showDebug) {
            DebugUtil.enableDrawOutside(this);
        }

        boolean hasTouchColor = !TextUtils.isEmpty(themeTouchColor);
        boolean hasBackgroundColor = !TextUtils.isEmpty(themeBackgroundColor) || !TextUtils.isEmpty(themeFallbackBackgroundColor);
        if (hasTouchColor && hasBackgroundColor) {
            setBackground(UI.getAdaptiveRippleDrawable(getBackgroundColor(theme).get(), getTouchColor(theme).get()));
        }
        else if (hasTouchColor) {
            ThemeColor touchColor = getTouchColor(theme);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                UI.setForegroundTouchFeedback(this, touchColor.get());
            }
            else {
                setBackground(UI.getAdaptiveRippleDrawable(Color.TRANSPARENT, touchColor.get()));
            }
        }
        else if (hasBackgroundColor){
            setBackgroundColor(getBackgroundColor(theme).get());
        }
    }

    private ThemeColor getTouchColor(Theme theme) {
        return ThemeableUtil.getThemeColor(theme, themeTouchColor, ThemeColor.WHITE);
    }

    private ThemeColor getBackgroundColor(Theme theme) {
        return getBackgroundColor(theme, ThemeColor.TRANSPARENT);
    }

    protected ThemeColor getBackgroundColor(Theme theme, ThemeColor fallback) {
        ThemeColor fallbackBackgroundColor = ThemeableUtil.getThemeColor(theme, themeFallbackBackgroundColor, fallback);
        return ThemeableUtil.getThemeColor(theme, themeBackgroundColor, fallbackBackgroundColor);
    }

    public void setThemeBackgroundColor(String themeBackgroundColor) {
        this.themeBackgroundColor = themeBackgroundColor;
    }

    public void setThemeFallbackBackgroundColor(String themeFallbackBackgroundColor) {
        this.themeFallbackBackgroundColor = themeFallbackBackgroundColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }
}
