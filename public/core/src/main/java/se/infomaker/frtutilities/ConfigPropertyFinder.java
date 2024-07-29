package se.infomaker.frtutilities;

import android.graphics.Color;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by magnusekstrom on 27/04/16.
 */
public class ConfigPropertyFinder {
    private static final String TAG = "ConfigPropertyFinder";

    private Map<String, Object> mConfig = new LinkedHashMap<>();

    public ConfigPropertyFinder() {

    }

    public ConfigPropertyFinder(Map<String, Object> config) {
        mConfig = config;
    }

    public void addConfig(Map<String, Object> config) {
        mConfig.putAll(config);
    }

    public Map<String, Object> getConfig() {
        return mConfig;
    }

    public <T> T getProperty(Class<T> type, String key) {
        Timber.d("mConfigPropertyFinder: %s ", mConfig);
        if (mConfig.containsKey(key)) {
            Object property = mConfig.get(key);
            if (type.isInstance(property)) {
                T value = type.cast(property);

                Timber.d("PropertyRetrieved for key %s: %s", key, value);

                return value;
            }
        }

        return null;
    }

    public <T> T getProperty(T defaultValue,  Class<T> type, String key) {
        T property = getProperty(type, key);
        return property != null ? property : defaultValue;
    }

    public <T> T getProperty(Class<T> type, String... key) {
        Map config = mConfig;
        for (int i = 0; i < key.length; i++) {
            if (config.containsKey(key[i])) {
                if (i < key.length - 1) {
                    Object property = config.get(key[i]);
                    if (property instanceof Map) {
                        config = (Map) property;
                    }
                } else {
                    Object property = config.get(key[i]);
                    if (type.isInstance(property)) {
                        return type.cast(property);
                    }
                }
            }
        }

        return null;
    }

    public <T> T getProperty(T defaultValue, Class<T> type, String... key) {
        T property = getProperty(type, key);
        return property != null ? property : defaultValue;
    }

    public ConfigPropertyFinder getPropertyFinder(String key) {
        if (mConfig.containsKey(key)) {
            Object property = mConfig.get(key);
            if (property instanceof Map) {
                return new ConfigPropertyFinder((Map<String, Object>) property);
            }
        }

        return new ConfigPropertyFinder();
    }

    public int getColor(String key) {
        String color = getProperty(String.class, key);
        return color != null ? Color.parseColor("#" + color) : Color.BLACK;
    }

    public int getColor(int defaultValue, String key) {
        String color = getProperty(String.class, key);
        return color != null ? Color.parseColor("#" + color) : defaultValue;
    }
}
