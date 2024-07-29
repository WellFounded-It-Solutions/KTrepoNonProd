package se.infomaker.iap.ui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import se.infomaker.iap.ui.promotion.Page;

public class ConfigLoader {

    /**
     * Load and parse config from an input stream
     * @param inputStream to parse from
     * @param clazz to parse to
     * @param <T> config class
     * @return configuration from stream
     */
    public static <T> T load(final InputStream inputStream, final Class<T> clazz) {
        try {
            if (inputStream != null) {
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Page.class, new PageDeserializer());
                builder.registerTypeAdapter(JSONObject.class, new JSONObjectDeserializer());
                final Gson gson = builder.create();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                return gson.fromJson(reader, clazz);
            }
        } catch (final Exception ignored) {
        }
        return null;
    }

    private static class JSONObjectDeserializer implements JsonDeserializer<JSONObject> {
        @Override
        public JSONObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return new JSONObject(json.toString());
            } catch (JSONException e) {
                return new JSONObject();
            }
        }
    }

    private static class PageDeserializer implements JsonDeserializer<Page> {
        @Override
        public Page deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return new Page(json.toString());
            } catch (JSONException e) {
                return new Page();
            }
        }
    }
}
