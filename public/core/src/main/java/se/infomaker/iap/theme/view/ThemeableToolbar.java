package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.navigaglobal.mobile.R;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;

public class ThemeableToolbar extends androidx.appcompat.widget.Toolbar implements Themeable {

    private String themeBackgroundColor;

    private DebugPainter debugPainter = new DebugPainter(DebugPainter.BLUE, DebugPainter.Position.BOTTOM_LEFT);
    private boolean showDebug = false;

    public ThemeableToolbar(Context context) {
        super(context);
        setupDebugMessage();
    }

    public ThemeableToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableToolbar);
        themeBackgroundColor = typedArray.getString(R.styleable.ThemeableToolbar_themeBackgroundColor);
        typedArray.recycle();
        setupDebugMessage();
    }

    private void setupDebugMessage() {
        if (themeBackgroundColor != null) {
            debugPainter.setDebugMessage("BC: " + themeBackgroundColor);
        }
    }

    @Override
    public void apply(Theme theme) {
        showDebug = ThemeManager.getInstance(getContext()).showDebug();
        if (showDebug) {
            DebugUtil.enableDrawOutside(this);
        }
        if (!TextUtils.isEmpty(themeBackgroundColor)) {
            setBackgroundColor(ThemeableUtil.getThemeColor(theme, themeBackgroundColor, ThemeColor.TRANSPARENT).get());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }
}
