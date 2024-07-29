package se.infomaker.frtutilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class JSONUtil {
    public static String getString(JSONObject object, String keyPath) throws JSONException {
        String[] parts = keyPath.split("\\.");
        JSONObject node = object;
        for (int i = 0; i < parts.length - 1; i++) {
            node = node.getJSONObject(parts[i]);
        }
        return node.getString(parts[parts.length - 1]);
    }

    public static String optString(JSONObject object, String keyPath) {
        return optString(object, keyPath, "");
    }

    public static String optString(JSONObject object, String keyPath, String fallback) {
        try {
            String[] parts = keyPath.split("\\.");
            JSONObject node = object;
            for (int i = 0; i < parts.length - 1; i++) {
                node = node.getJSONObject(parts[i]);
            }
            return node.optString(parts[parts.length - 1], fallback);
        } catch (JSONException e) {
            return fallback;
        }
    }

    public static JSONArray optJSONArray(JSONObject object, String keyPath) {
        try {
            return getJSONArray(object, keyPath);
        } catch (JSONException e) {

        }
        return null;
    }

    public static JSONObject optJSONObject(JSONObject object, String keyPath) {
        try {
            return getJSONObject(object, keyPath);
        } catch (JSONException e) {

        }
        return null;
    }

    public static final boolean getBoolean(JSONObject object, String keyPath) throws JSONException {
        String[] parts = keyPath.split("\\.");
        JSONObject node = object;
        for (int i = 0; i < parts.length - 1; i++) {
            node = node.getJSONObject(parts[i]);
        }
        return node.getBoolean(parts[parts.length - 1]);
    }

    public static boolean optBoolean(JSONObject object, String keyPath, boolean defaultValue) {
        try {
            return getBoolean(object, keyPath);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static JSONObject getJSONObject(JSONObject object, String keyPath) throws JSONException {
        String[] parts = keyPath.split("\\.");
        JSONObject node = object;
        for (int i = 0; i < parts.length - 1; i++) {
            node = node.getJSONObject(parts[i]);
        }
        return node.getJSONObject(parts[parts.length - 1]);
    }

    public static JSONArray getJSONArray(JSONObject object, String keyPath) throws JSONException {
        String[] parts = keyPath.split("\\.");
        JSONObject node = object;
        for (int i = 0; i < parts.length - 1; i++) {
            node = node.getJSONObject(parts[i]);
        }
        return node.getJSONArray(parts[parts.length - 1]);
    }

    public static void put(JSONObject object, String keyPath, String value) {
        put(object, keyPath, (Object) value);
    }

    public static void put(JSONObject object, String keyPath, Object value) {
        String[] parts = keyPath.split("\\.");
        JSONObject node = object;
        for (int i = 0; i < parts.length - 1; i++) {
            try {
                node = node.getJSONObject(parts[i]);
            } catch (JSONException e) {
                JSONObject next = new JSONObject();
                try {
                    node.put(parts[i], next);
                    node = next;
                } catch (JSONException e1) {
                    Timber.e(e1, "Failed to create node");
                }
            }
        }
        try {
            node.put(parts[parts.length - 1], value);
        } catch (JSONException e) {
            Timber.e(e, "Failed to put value %s", value);
        }
    }

    public static JsonObject toJsonObject(JSONObject object) {
        return object != null ? new JsonParser().parse(object.toString()).getAsJsonObject() : null;
    }

    public static JSONObject toJSONObject(JsonObject object) throws JSONException {
        return new JSONObject(object.toString());
    }

    public static JSONObject wrap(String keyPath, Object object) throws JSONException {
        String[] parts = keyPath.split("\\.");
        JSONObject out = new JSONObject();
        JSONObject node = out;
        for (int i = 0; i < parts.length - 1; i++) {
            JSONObject current = new JSONObject();
            node.put(parts[i], current);
            node = current;
        }

        node.put(parts[parts.length - 1], object);
        return out;
    }

    /**
     * Convert
     *
     * @param object
     * @return Map of JSONObject content
     */
    public static Map<String, String> toMap(JSONObject object) {
        Iterator<String> keys = object.keys();
        HashMap<String, String> map = new HashMap<>();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, object.optString(key));
        }
        return map;
    }

    public static List<String> toStringList(JSONArray jsonArray) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }
}
