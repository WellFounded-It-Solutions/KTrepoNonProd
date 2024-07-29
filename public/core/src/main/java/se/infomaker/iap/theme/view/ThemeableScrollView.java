package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.navigaglobal.mobile.R;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.util.UI;

public class ThemeableScrollView extends NestedScrollView implements Themeable {
    private String themeBackgroundColor;
    private String themeTouchColor;

    private DebugPainter debugPainter = new DebugPainter(DebugPainter.BLUE, DebugPainter.Position.BOTTOM_LEFT);
    private boolean showDebug = false;

    public ThemeableScrollView(@NonNull Context context) {
        super(context);
    }

    public ThemeableScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableScrollView);
        themeTouchColor = typedArray.getString(R.styleable.ThemeableScrollView_themeTouchColor);
        themeBackgroundColor = typedArray.getString(R.styleable.ThemeableScrollView_themeBackgroundColor);
        typedArray.recycle();
    }

    private void setupDebugMessage() {
        if (showDebug) {
            StringBuilder builder = new StringBuilder();
            DebugUtil.write(builder, "BC: ", themeBackgroundColor, true);
            DebugUtil.write(builder, "TC: ", themeTouchColor, true);
            debugPainter.setDebugMessage(builder.toString());
        }
    }

    @Override
    public void apply(Theme theme) {
        showDebug = ThemeManager.getInstance(getContext()).showDebug();
        if (showDebug) {
            setupDebugMessage();
            DebugUtil.enableDrawOutside(this);
        }
        if (!TextUtils.isEmpty(themeTouchColor)) {
            UI.setForegroundTouchFeedback(this, ThemeableUtil.getThemeColor(theme, themeTouchColor, ThemeColor.WHITE).get());
        }
        if (!TextUtils.isEmpty(themeBackgroundColor)){
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
