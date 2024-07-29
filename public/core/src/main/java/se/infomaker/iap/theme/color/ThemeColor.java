package se.infomaker.iap.theme.color;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ThemeColor {
    public static final ThemeColor BLACK = new ThemeColor(Color.BLACK);
    public static final ThemeColor DKGRAY = new ThemeColor(Color.DKGRAY);
    public static final ThemeColor GRAY = new ThemeColor(Color.GRAY);
    public static final ThemeColor LTGRAY = new ThemeColor(Color.LTGRAY);
    public static final ThemeColor WHITE = new ThemeColor(Color.WHITE);
    public static final ThemeColor RED = new ThemeColor(Color.RED);
    public static final ThemeColor GREEN = new ThemeColor(Color.GREEN);
    public static final ThemeColor BLUE = new ThemeColor(Color.BLUE);
    public static final ThemeColor YELLOW = new ThemeColor(Color.YELLOW);
    public static final ThemeColor CYAN = new ThemeColor(Color.CYAN);
    public static final ThemeColor MAGENTA = new ThemeColor(Color.MAGENTA);
    public static final ThemeColor TRANSPARENT = new ThemeColor(Color.TRANSPARENT);
    public static final ThemeColor DEFAULT_BACKGROUND_COLOR = new ThemeColor(0xFFF7F7F7);
    public static final ThemeColor DEFAULT_LIST_BACKGROUND_COLOR = ThemeColor.WHITE;
    public static final ThemeColor DEFAULT_CHROME_COLOR = ThemeColor.WHITE;
    public static final ThemeColor DEFAULT_ON_CHROME_COLOR = ThemeColor.BLACK;
    public static final ThemeColor DEFAULT_BRAND_COLOR = new ThemeColor(0xFF6A30CB);
    public static final ThemeColor DEFAULT_ON_BRAND_COLOR = ThemeColor.WHITE;
    public static final ThemeColor DEFAULT_TEXT_COLOR = new ThemeColor(0xFF212121);

    private final int color;
    private Drawable drawable;

    public ThemeColor(int color) {
        this.color = color;
    }

    public Drawable asDrawable() {
        if (drawable == null) {
            drawable = new ColorDrawable(color);
        }
        return drawable;
    }

    public int get() {
        return color;
    }

    public void apply(TextView textView) {
        textView.setTextColor(color);
    }

    public void paint(TextPaint paint) {
        paint.setColor(color);
    }

    @Override
    public int hashCode() {
        return color;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThemeColor)) {
            return false;
        }
        ThemeColor other = (ThemeColor) obj;
        return color == other.color;
    }

    @NonNull
    @Override
    public String toString() {
        return "ThemeColor(color=" + color + ")";
    }
}
