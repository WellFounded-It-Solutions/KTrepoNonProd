package se.infomaker.iap.theme.image;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

public class ThemeImage {

    public static final ThemeImage DEFAULT = new ThemeImage(new ColorDrawable(Color.TRANSPARENT));
    private final int resourceId;
    private Drawable drawable;

    public ThemeImage(int resourceId) {
        this.resourceId = resourceId;
    }

    public ThemeImage(ColorDrawable drawable) {
        this.drawable = drawable;
        resourceId = 0;
    }

    public int getResourceId() {
        return resourceId;
    }

    public Drawable getImage(Context context) {
        if (drawable != null) {
            return drawable;
        }
        return AppCompatResources.getDrawable(context, resourceId);
    }

    @Override
    public int hashCode() {
        return resourceId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThemeImage)) {
            return false;
        }
        ThemeImage other = (ThemeImage) obj;
        return resourceId == other.resourceId;
    }

    @NonNull
    @Override
    public String toString() {
        return "ThemeImage(resourceId=" + resourceId + ")";
    }
}
