package se.infomaker.frtutilities.meta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

import io.reactivex.Observable;

/**
 * Provides values on a keypath basis
 */
public interface ValueProvider extends Serializable {

    /**
     *
     * @param keyPath to get values for
     * @return a list of values or null if no values ar present
     */
    @Nullable
    List<String> getStrings(@NonNull String keyPath);

    /**
     *
     * @param keyPath to get value for
     * @return value or null if not present
     */
    @Nullable
    String getString(@NonNull String keyPath);

    /**
     *
     * @param keyPath to get value for
     * @return Observable for the given keypath
     */
    @Nullable
    Observable<String> observeString(@NonNull String keyPath);
}
