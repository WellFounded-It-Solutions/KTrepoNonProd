package se.infomaker.iap.theme.transforms;

import android.text.SpannableStringBuilder;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class ThemeTransforms {
    public enum Transforms {
        UPPERCASE,
        CAPITALIZE
    }

    public static final ThemeTransforms DEFAULT = new ThemeTransforms(new ArrayList<Transforms>());

    public List<Transforms> getTransforms() {
        return transforms;
    }

    private final List<Transforms> transforms;

    public ThemeTransforms(List<Transforms> transforms) {
        this.transforms = transforms;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public void apply(TextView view) {
        view.setAllCaps(transforms.contains(Transforms.UPPERCASE));

        if (transforms.contains(Transforms.CAPITALIZE)) {
            String toCapitalise = (String) view.getText();
            view.setText(capitalize(toCapitalise));
        }
    }

    public SpannableStringBuilder apply(SpannableStringBuilder stringBuilder, int start, int end) {
        if (transforms.isEmpty()) {
            return stringBuilder;
        }
        for (Transforms transform: transforms) {
            String substring = stringBuilder.toString().substring(start, end);
            switch (transform) {
                case CAPITALIZE:
                    stringBuilder.replace(start, end, capitalize(substring));
                    break;
                case UPPERCASE:
                    stringBuilder.replace(start, end, substring.toUpperCase());
                    break;
                default:
                    Timber.d("Invalid transform.");
                    break;
            }
        }
        return stringBuilder;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(transforms);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThemeTransforms)) {
            return false;
        }
        ThemeTransforms other = (ThemeTransforms) obj;
        return Objects.equals(transforms, other.transforms);
    }

    @NonNull
    @Override
    public String toString() {
        return "ThemeTransforms(transforms=" + transforms + ")";
    }
}
