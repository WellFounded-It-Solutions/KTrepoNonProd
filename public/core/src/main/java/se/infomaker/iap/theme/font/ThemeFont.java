package se.infomaker.iap.theme.font;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class ThemeFont {
    public static final ThemeFont DEFAULT = new ThemeFont(Typeface.DEFAULT);

    private final Typeface typeface;

    public ThemeFont(Typeface typeface) {
        this.typeface = typeface;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void apply(TextView textView) {
        textView.setTypeface(typeface);
    }

    public void paint(TextPaint paint) {
        paint.setTypeface(typeface);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(typeface);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThemeFont)) {
            return false;
        }
        ThemeFont other = (ThemeFont) obj;
        return Objects.equals(typeface, other.typeface);
    }

    @NonNull
    @Override
    public String toString() {
        return "ThemeFont(typeface=" + typeface + ")";
    }
}
