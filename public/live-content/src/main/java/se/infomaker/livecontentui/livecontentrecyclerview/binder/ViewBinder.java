package se.infomaker.livecontentui.livecontentrecyclerview.binder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;

/**
 * Binds a value to a view
 */
public interface ViewBinder {
    /**
     * Bind value to view with properties
     * @param view
     * @param value
     * @param properties
     */
    @Nullable
    LiveBinding bind(@NonNull final View view, @Nullable String value, @NonNull PropertyObject properties);

    /**
     *
     * @return all supported view binders
     */
    @NonNull
    Set<Class> supportedViews();

    /**
     * Gets the key to identify, IMTextView and IMImageView uses id
     * while IMFrameLayout uses attribute propertyKey
     * @return The identifying key or null if no key found
     */
    @Nullable
    String getKey(@NonNull View view);
}
