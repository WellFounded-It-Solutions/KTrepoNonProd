package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigaglobal.mobile.R;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.util.UI;

public class ThemeableFrameLayout extends FrameLayout implements Themeable {
    private String themeTouchColor;
    private String themeBackgroundColor;
    private String themeFallbackBackgroundColor;

    private DebugPainter debugPainter = new DebugPainter(DebugPainter.BLUE, DebugPainter.Position.BOTTOM_LEFT);
    private boolean showDebug = false;

    public ThemeableFrameLayout(@NonNull Context context) {
        super(context);
    }

    public ThemeableFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableFrameLayout);
        themeTouchColor = typedArray.getString(R.styleable.ThemeableFrameLayout_themeTouchColor);
        themeBackgroundColor = typedArray.getString(R.styleable.ThemeableFrameLayout_themeBackgroundColor);
        themeFallbackBackgroundColor = typedArray.getString(R.styleable.ThemeableFrameLayout_themeFallbackBackgroundColor);
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
        if (!TextUtils.isEmpty(themeTouchColor)) {
            UI.setForegroundTouchFeedback(this, ThemeableUtil.getThemeColor(theme, themeTouchColor, ThemeColor.WHITE).get());
        }
        if (!TextUtils.isEmpty(themeBackgroundColor) || !TextUtils.isEmpty(themeFallbackBackgroundColor)) {
            setBackgroundColor(getBackgroundColor(theme).get());
        }
    }

    private ThemeColor getBackgroundColor(Theme theme) {
        ThemeColor fallback = ThemeableUtil.getThemeColor(theme, themeFallbackBackgroundColor, ThemeColor.TRANSPARENT);
        return ThemeableUtil.getThemeColor(theme, themeBackgroundColor, fallback);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }
}
