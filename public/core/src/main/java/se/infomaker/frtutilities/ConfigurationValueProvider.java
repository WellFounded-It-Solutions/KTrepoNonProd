package se.infomaker.frtutilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import se.infomaker.frtutilities.meta.ValueProvider;
import timber.log.Timber;

final class ConfigurationValueProvider implements ValueProvider{
    private static final String CONFIGURATION = "CONFIGURATION";
    private transient Map<String, JSONObject> JSONConfigs = new HashMap<>();

    @Nullable
    @Override
    public List<String> getStrings(@NonNull String keyPath) {
        String value = getString(keyPath);
        if (value != null) {
            ArrayList<String> list = new ArrayList<>();
            list.add(value);
            return list;
        }
        return null;
    }

    @Nullable
    @Override
    public String getString(@NonNull String keyPath) {
        String[] parts = keyPath.split("\\.");
        if (parts.length > 2 && CONFIGURATION.equals(parts[0])) {
            JSONObject config = getJSONConfig(parts[1]);
            if (config != null) {
                String configKeyPath = keyPath.substring(CONFIGURATION.length() + 2 + parts[1].length());
                return JSONUtil.optString(config, configKeyPath);
            }
        }

        return null;
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

    /**
     * Returns the module configuration as a JSONObject, uses a cache to prevent object creation
     * @param moduleId
     * @return
     */
    private JSONObject getJSONConfig(@NonNull String moduleId) {
        if (JSONConfigs == null) {
            JSONConfigs = new HashMap<>();
        }
        if (JSONConfigs.containsKey(moduleId)) {
            return JSONConfigs.get(moduleId);
        }
        String configJson = ConfigManager.getInstance().getConfigJson(moduleId);
        if(configJson != null) {
            try {
                JSONConfigs.put(moduleId, new JSONObject(configJson));
                return JSONConfigs.get(moduleId);
            } catch (JSONException e) {
                Timber.e(e, "Invalid configuration");
            }
        }
        return null;
    }
}
