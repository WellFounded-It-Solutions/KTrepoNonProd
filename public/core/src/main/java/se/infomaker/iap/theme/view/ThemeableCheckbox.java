package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.navigaglobal.mobile.R;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.color.ThemeColorParser;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;
import se.infomaker.iap.theme.util.UI;


public class ThemeableCheckbox extends AppCompatCheckBox implements Themeable {

    private List<String> themeKeys;
    private String themeTouchColor;

    private DebugPainter debugPainter = new DebugPainter(DebugPainter.GREEN, DebugPainter.Position.TOP_RIGHT);
    private boolean showDebug = false;

    public ThemeableCheckbox(Context context) {
        super(context);
    }

    public ThemeableCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableCheckbox);
        String themeKey = typedArray.getString(R.styleable.ThemeableCheckbox_themeKey);
        if (themeKey != null) {
            themeKeys = new ArrayList<>();
            themeKeys.add(themeKey);
        }
        themeTouchColor = typedArray.getString(R.styleable.ThemeableCheckbox_themeTouchColor);
        typedArray.recycle();
    }

    private void setupDebugMessage() {
        if (showDebug) {
            StringBuilder builder = new StringBuilder();
            DebugUtil.writeGroups(builder, "T: ", themeKeys, true);
            DebugUtil.write(builder, " TC: ", themeTouchColor, true);
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
        if (themeKeys != null) {
            ThemeTextStyle text = theme.getText(themeKeys, null);
            if (text != null) {
                text.apply(theme, this);
            }
        }
        if (!TextUtils.isEmpty(themeTouchColor)) {
            ThemeColor touchColor;
            try {
                touchColor = new ThemeColorParser().parseObject(themeTouchColor);
            } catch (AttributeParseException e) {
                touchColor = theme.getColor(themeTouchColor, ThemeColor.WHITE);
            }
            UI.setTouchFeedback(this, touchColor.get());
        }
    }

    public void setThemeKey(String themeKey) {
        themeKeys = new ArrayList<>();
        themeKeys.add(themeKey);
    }

    public void setThemeKeys(List<String> themeKeys) {
        this.themeKeys = themeKeys;
    }

    public void setThemeTouchColor(String themeTouchColor) {
        this.themeTouchColor = themeTouchColor;
    }

    public List<String> getThemeKeys() {
        return themeKeys;
    }

    public String getThemeTouchColor() {
        return themeTouchColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }
}
