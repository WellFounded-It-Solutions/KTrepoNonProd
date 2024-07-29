package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.button.MaterialButton;
import com.navigaglobal.mobile.R;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;

public class ThemeableMaterialButton extends MaterialButton implements Themeable {

    private List<String> themeKeys;
    private String themeTouchColor;
    private String themeBackgroundColor;
    private String themeStrokeColor;
    private String themeDisabledColor;
    private String themeFallbackBackgroundColor;

    private DebugPainter debugPainter = new DebugPainter(DebugPainter.GREEN, DebugPainter.Position.TOP_RIGHT);
    private boolean showDebug = false;

    public ThemeableMaterialButton(Context context) {
        super(context);
    }

    public ThemeableMaterialButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableMaterialButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs);
    }


    private void init(AttributeSet attrs) {

        TypedArray buttonTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableMaterialButton);
        String themeKey = buttonTypedArray .getString(R.styleable.ThemeableMaterialButton_themeKey);
        themeStrokeColor = buttonTypedArray.getString(R.styleable.ThemeableMaterialButton_themeStrokeColor);
        themeDisabledColor = buttonTypedArray.getString(R.styleable.ThemeableMaterialButton_themeDisabledColor);

        themeKeys = new ArrayList<>();
        if (themeKey != null) {

            themeKeys.add(themeKey);
        }
        else {
            themeKeys.add("buttonText");
        }
        themeTouchColor = buttonTypedArray.getString(R.styleable.ThemeableMaterialButton_themeTouchColor);
        themeBackgroundColor = buttonTypedArray.getString(R.styleable.ThemeableMaterialButton_themeBackgroundColor);
        if (themeBackgroundColor == null) {
            themeBackgroundColor = "buttonBackground";
        }
        themeFallbackBackgroundColor = buttonTypedArray.getString(R.styleable.ThemeableMaterialButton_themeFallbackBackgroundColor);

        buttonTypedArray.recycle();
        setupDebugMessage();
    }

    private void setupDebugMessage() {
        if (showDebug) {
            StringBuilder builder = new StringBuilder();
            DebugUtil.writeGroups(builder, "T: ", themeKeys, false);
            DebugUtil.write(builder, " BC: ", themeBackgroundColor, true);
            DebugUtil.write(builder, " FBC: ", themeFallbackBackgroundColor, true);
            DebugUtil.write(builder, " TC: ", themeTouchColor, true);
            DebugUtil.write(builder, " StC: ", themeStrokeColor, true);
            DebugUtil.write(builder, " DC: ", themeDisabledColor, true);
            debugPainter.setDebugMessage(builder.toString());
        }
    }

    @Override
    public void apply(@NonNull Theme theme) {
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

        if (!TextUtils.isEmpty(themeStrokeColor)) {
            int borderColor = ThemeableUtil.getThemeColor(theme, themeStrokeColor, ThemeColor.TRANSPARENT).get();
            setStrokeColor(ColorStateList.valueOf(borderColor));
        }

        int backgroundColor = getBackgroundColor(theme).get();
        int touchColor;
        if (TextUtils.isEmpty(themeTouchColor)) {
            if (ThemeUtils.isDarkColor(backgroundColor)) {
                touchColor = ColorUtils.blendARGB(backgroundColor, Color.WHITE, 0.5f);
            }
            else {
                touchColor = ColorUtils.blendARGB(backgroundColor, Color.BLACK, 0.3f);
            }

        } else {
            touchColor = ThemeableUtil.getThemeColor(theme, themeTouchColor, ThemeColor.TRANSPARENT).get();
        }

        ThemeColor disabledThemeColor = ThemeableUtil.getThemeColor(theme, themeDisabledColor, null);
        int disabledColor = disabledThemeColor != null ? disabledThemeColor.get() : ContextCompat.getColor(getContext(), R.color.buttonColorDisabled);

        setBackgroundTintList(getButtonColorStateList(backgroundColor, disabledColor));
        setRippleColor(ColorStateList.valueOf(touchColor));
    }

    private ThemeColor getBackgroundColor(Theme theme) {
        ThemeColor fallback = ThemeableUtil.getThemeColor(theme, themeFallbackBackgroundColor, ThemeColor.TRANSPARENT);
        return ThemeableUtil.getThemeColor(theme, themeBackgroundColor, fallback);
    }

    public List<String> getThemeKeys() {
        return themeKeys;
    }

    public void setThemeKeys(List<String> themeKeys) {
        this.themeKeys = themeKeys;
    }

    public String getThemeTouchColor() {
        return themeTouchColor;
    }

    public void setThemeTouchColor(String themeTouchColor) {
        this.themeTouchColor = themeTouchColor;
    }

    public String getThemeBackgroundColor() {
        return themeBackgroundColor;
    }

    public void setThemeBackgroundColor(String themeBackgroundColor) {
        this.themeBackgroundColor = themeBackgroundColor;
    }

    public String getThemeStrokeColor() {
        return themeStrokeColor;
    }

    public void setThemeStrokeColor(String themeStrokeColor) {
        this.themeStrokeColor = themeStrokeColor;
    }

    private static ColorStateList getButtonColorStateList(int color, int disabled) {
        return new ColorStateList(
                new int[][]{{android.R.attr.state_enabled}, {-android.R.attr.state_enabled}},
                new int[]{color, disabled});
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }
}
