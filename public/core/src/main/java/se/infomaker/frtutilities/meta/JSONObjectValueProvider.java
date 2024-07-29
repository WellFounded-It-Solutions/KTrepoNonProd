package se.infomaker.frtutilities.meta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import se.infomaker.frtutilities.CollectionUtil;
import se.infomaker.frtutilities.JSONUtil;

public class JSONObjectValueProvider implements ValueProvider {
    private final JSONObject object;

    public JSONObjectValueProvider(@NonNull JSONObject object) {
        this.object = object;
    }

    @Nullable
    @Override
    public List<String> getStrings(@NonNull String keyPath) {
        try {
            return JSONUtil.toStringList(JSONUtil.getJSONArray(object, keyPath));
        } catch (JSONException e) {
            try {
                return CollectionUtil.asList(JSONUtil.getString(object, keyPath));
            } catch (JSONException e1) {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public String getString(@NonNull String keyPath) {
        return JSONUtil.optString(object, keyPath, null);
    }

    @Nullable
    @Override
    public Observable<String> observeString(@NonNull String keyPath) {
        String value = getString(keyPath);
        if (value == null) {
            return Observable.never();
        }
        return Observable.just(value);
    }
}
