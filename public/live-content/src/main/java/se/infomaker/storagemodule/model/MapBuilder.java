package se.infomaker.storagemodule.model;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder {

    private final HashMap<String, String> map;

    public MapBuilder() {
        this.map = new HashMap<>();
    }

    public MapBuilder put(String key, String value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder put(String key, Double value) {
        map.put(key, Double.toString(value));
        return this;
    }

    public MapBuilder put(String key, Float value) {
        map.put(key, Float.toString(value));
        return this;
    }

    public MapBuilder put(String key, Integer value) {
        map.put(key, Integer.toString(value));
        return this;
    }

    public Map<String, String> create() {
        Map<String, String> out = new HashMap<>();
        out.putAll(map);
        return out;
    }
}
