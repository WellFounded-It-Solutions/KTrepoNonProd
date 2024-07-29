package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.OneShotPreDrawListener;

import com.google.android.material.tabs.TabLayout;
import com.navigaglobal.mobile.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;

public class ThemeableTabLayout extends TabLayout implements Themeable {

    private List<String> themeKeys;
    private Theme theme;
    private String backgroundColor;
    private String selectedColor;
    private String deselectedColor;

    private final DebugPainter debugPainter = new DebugPainter(DebugPainter.BLUE, DebugPainter.Position.BOTTOM_LEFT);
    private boolean showDebug = false;

    public ThemeableTabLayout(Context context) {
        super(context);
        init(null);
    }

    public ThemeableTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableTabLayout);
            String themeKey = typedArray.getString(R.styleable.ThemeableTabLayout_themeKey);
            backgroundColor = typedArray.getString(R.styleable.ThemeableTabLayout_themeBackgroundColor);
            selectedColor = typedArray.getString(R.styleable.ThemeableTabLayout_themeSelectedColor);
            deselectedColor = typedArray.getString(R.styleable.ThemeableTabLayout_themeDeselectedColor);

            if (themeKey != null) {
                themeKeys = new ArrayList<>();
                themeKeys.add(themeKey);
            }
            typedArray.recycle();

            StringBuilder builder = new StringBuilder();
            if (backgroundColor != null) {
                builder.append("BC: ").append(backgroundColor);
            }
            if (selectedColor != null) {
                builder.append(" SC: ").append(selectedColor);
            }
            builder.append(" TC: touchfeedback");
            debugPainter.setDebugMessage(builder.toString());
        }
    }

    public Tab newTab() {
        Tab tab = super.newTab();
        tab.setCustomView(R.layout.themeable_tab);
        if (theme != null) {
            ThemeTextStyle textStyle = null;
            if (themeKeys != null) {
                textStyle = theme.getText(themeKeys, null);
                if (textStyle != null) {
                    applyTextStyleToTab(theme, textStyle, tab);
                }
            }
            if (deselectedColor != null) {
                ThemeColor deselectedColor = theme.getColor(this.deselectedColor, null);
                if (deselectedColor != null) {
                    int selectedColorCode = findSelectedColor(theme, textStyle);
                    int deselectedColorCode = deselectedColor.get();
                    ColorStateList colors = createColorStateList(deselectedColorCode, selectedColorCode);
                    applyColorsToTab(colors, tab);
                }
            }
        }
        return tab;
    }

    @Override
    public void apply(@NotNull Theme theme) {
        showDebug = ThemeManager.getInstance(getContext()).showDebug();
        if (showDebug) {
            DebugUtil.enableDrawOutside(this);
        }
        this.theme = theme;
        if (!TextUtils.isEmpty(backgroundColor)) {
            setBackgroundColor(theme.getColor(backgroundColor, ThemeColor.TRANSPARENT).get());
        }
        if (!TextUtils.isEmpty(selectedColor)) {
            setSelectedTabIndicatorColor(theme.getColor(selectedColor, ThemeColor.LTGRAY).get());
        }

        int[][] states = new int[][]{
                new int[]{}  // normal
        };
        int[] colors = new int[]{
                theme.getColor("touchfeedback", ThemeColor.LTGRAY).get()
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setTabRippleColor(colorStateList);

        OneShotPreDrawListener.add(this, () -> {
            ThemeTextStyle textStyle = null;
            if (themeKeys != null) {
                textStyle = theme.getText(themeKeys, null);
                if (textStyle != null) {
                    applyTextStyleToAllTabs(theme, textStyle);
                }
            }

            applyDeselectedTextColor(theme, textStyle);
        });
    }

    private void applyTextStyleToAllTabs(Theme theme, ThemeTextStyle textStyle) {
        for (int i = 0; i < getTabCount(); i++) {
            Tab tab = getTabAt(i);
            if (tab != null) {
                applyTextStyleToTab(theme, textStyle, tab);
            }
        }
    }

    private void applyTextStyleToTab(Theme theme, ThemeTextStyle textStyle, Tab tab) {
        TextView textView = ((TextView) tab.getCustomView());
        if (textView != null) {
            textStyle.apply(theme, textView);
        }
    }

    private void applyDeselectedTextColor(Theme theme, ThemeTextStyle textStyle) {
        if (deselectedColor != null) {
            ThemeColor deselectedColor = theme.getColor(this.deselectedColor, null);
            if (deselectedColor != null) {
                int selectedColorCode = findSelectedColor(theme, textStyle);
                int deselectedColorCode = deselectedColor.get();
                ColorStateList colors = createColorStateList(deselectedColorCode, selectedColorCode);
                applyColorsToAllTabs(colors);
            }
        }
    }

    private void applyColorsToAllTabs(ColorStateList colors) {
        for (int i = 0; i < getTabCount(); i++) {
            Tab tab = getTabAt(i);
            if (tab != null) {
                applyColorsToTab(colors, tab);
            }
        }
    }

    private void applyColorsToTab(ColorStateList colors, Tab tab) {
        TextView textView = ((TextView) tab.getCustomView());
        if (textView != null) {
            textView.setTextColor(colors);
        }
    }

    private int findSelectedColor(Theme theme, ThemeTextStyle textStyle) {
        ThemeColor selectedTextColor = ThemeColor.BLACK;
        if (textStyle != null) {
            selectedTextColor = textStyle.getColor(theme);
        }
        return selectedTextColor.get();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }

    @NonNull
    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        states[i] = SELECTED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;

        return new ColorStateList(states, colors);
    }
}
