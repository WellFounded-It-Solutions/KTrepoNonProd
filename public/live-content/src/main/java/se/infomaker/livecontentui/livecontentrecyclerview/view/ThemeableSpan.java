package se.infomaker.livecontentui.livecontentrecyclerview.view;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.DisplayMetrics;

import java.util.List;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;

public class ThemeableSpan extends MetricAffectingSpan {
    private Theme theme;
    private final List<String> themeKeys;

    ThemeableSpan(Theme theme, List<String> themeKeys) {
        this.themeKeys = themeKeys;
        this.theme = theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        applyTheme(textPaint);
    }

    @Override
    public void updateMeasureState(TextPaint textPaint) {
        applyTheme(textPaint);
    }

    private void applyTheme(TextPaint paint) {
        if (theme != null) {
            ThemeTextStyle text = theme.getText(themeKeys, null);
            if (text != null) {
                text.paint(theme, paint);
            }
        }
    }
}
