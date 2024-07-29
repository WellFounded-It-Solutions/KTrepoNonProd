package se.infomaker.livecontentui.section.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import se.infomaker.frtutilities.meta.ValueProvider;

public class SingleValueProvider implements ValueProvider {

    private final String key;
    private final String value;

    public SingleValueProvider(@NonNull String key, @NonNull String value) {
        this.key = key;
        this.value = value;
    }

    @Nullable
    @Override
    public List<String> getStrings(@NonNull String keyPath) {
        if (key.equals(keyPath)) {
            List<String> list = new ArrayList<>();
            list.add(value);
            return list;
        }
        return null;
    }

    @Nullable
    @Override
    public String getString(@NonNull String keyPath) {
        return key.equals(keyPath) ? value : null;
    }

    @Nullable
    @Override
    public Observable<String> observeString(@NonNull String keyPath) {
        return key.equals(keyPath) ? Observable.just(value) : Observable.never();
    }
}
