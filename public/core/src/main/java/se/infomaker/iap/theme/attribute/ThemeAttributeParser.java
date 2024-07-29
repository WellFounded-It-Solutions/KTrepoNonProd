package se.infomaker.iap.theme.attribute;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import se.infomaker.iap.theme.Resolver;
import se.infomaker.iap.theme.util.SetUtil;
import timber.log.Timber;

/**
 * Parse theme values and references and provide
 * @param <T> type to parse 
 */
public abstract class ThemeAttributeParser<T> {
    private static final String ANDROID_OVERRIDES_KEY = "android";
    private static final String IOS_OVERRIDES_KEY = "ios";

    private final Map<String, T> values;
    private final Map<String, String> references;

    public ThemeAttributeParser() {
        this.values = new HashMap<>();
        this.references = new HashMap<>();
    }

    /**
     * Parse values, references and platform overrides,
     * The parsed output is consumed by the parser and inserted when resolver is created
     * @param values to parse
     * @return the parser to chain calls
     */
    public ThemeAttributeParser<T> parse(JSONObject values) {
        if (values == null) {
            return this;
        }
        JSONObject overrides = values.optJSONObject(ANDROID_OVERRIDES_KEY);
        values.remove(ANDROID_OVERRIDES_KEY);
        values.remove(IOS_OVERRIDES_KEY);
        parseValues(values);
        parseValues(overrides);
        return this;
    }

    private void parseValues(JSONObject values) {
        if (values == null) {
            return;
        }
        Set<String> keys = SetUtil.setFrom(values.keys());
        for (String key : keys) {
            try {
                String objectValue = values.getString(key);
                if (isValueObject(objectValue)) {
                    this.values.put(key, parseObject(objectValue));
                } else {
                    references.put(key, objectValue);
                }
            } catch (JSONException | AttributeParseException e) {
                Timber.e(e, "Could not parse theme attribute");
            }
        }
    }

    /**
     * Determines if the value should be parsed as a value object
     * @param value to parse
     * @return true if the value is a value object, false if it's a reference
     */
    @SuppressWarnings("WeakerAccess")
    public abstract boolean isValueObject(Object value);

    /**
     * Parse a value to an object
     * @param value to parse
     * @return parsed value
     * @throws AttributeParseException if the value could not be parsed
     */
    public abstract T parseObject(Object value) throws AttributeParseException;

    public Resolver<T> createResolver() {
        return new Resolver<>(values, references);
    }
}
