package se.infomaker.iap.theme.size;

import android.content.res.Resources;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ThemeSize {
    private static final int ZERO_SIZE = 0;
    private static final int DEFAULT_SIZE = 16;
    public static final ThemeSize ZERO = new ThemeSize(ZERO_SIZE);
    public static final ThemeSize DEFAULT = new ThemeSize(DEFAULT_SIZE);

    private final float size;

    public ThemeSize(float size) {
        this.size = size;
    }

    /**
     * @return The size without conversion
     */
    public float getSize() {
        return size;
    }

    /**
     * @return The size converted from dp to px
     */
    public float getSizePx() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = size * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    /**
     * @return The size converted from px to dp
     */
    public float getSizeDp() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = size / (metrics.densityDpi / 160f);
        return Math.round(dp);

    }

    public void apply(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void paint(TextPaint paint) {
        float fontSizeSP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, Resources.getSystem().getDisplayMetrics());
        paint.setTextSize(fontSizeSP);
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(size);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThemeSize)) {
            return false;
        }
        ThemeSize other = (ThemeSize) obj;
        return Float.compare(size, other.size) == 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "ThemeSize(size=" + size + ")";
    }
}