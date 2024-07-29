package se.infomaker.iap.theme.linespacing;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import se.infomaker.iap.theme.util.UI;

public class ThemeLineSpacing {
    public static final int DEFAULT_MULTIPLIER = 1;
    public static final int DEFAULT_EXTRA = 0;
    public static final ThemeLineSpacing DEFAULT = new ThemeLineSpacing(DEFAULT_MULTIPLIER, DEFAULT_EXTRA);

    private final float multiplier;
    private final float extra;

    public ThemeLineSpacing(float multiplier, float extra) {
        this.multiplier = multiplier;
        this.extra = extra;
    }

    public void apply(TextView textView) {
        textView.setLineSpacing(UI.dp2px(extra), multiplier);
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(multiplier) * 31
                + Float.floatToIntBits(extra);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThemeLineSpacing)) {
            return false;
        }
        ThemeLineSpacing other = (ThemeLineSpacing) obj;
        return Float.compare(this.multiplier, other.multiplier) == 0
                && Float.compare(this.extra, other.extra) == 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "ThemeLineSpacing(multiplier=" + multiplier
                + ", extra=" + extra
                + ")";
    }
}
