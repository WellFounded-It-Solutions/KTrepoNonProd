package se.infomaker.iap.theme.linespacing;

import org.json.JSONException;
import org.json.JSONObject;

import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.attribute.ThemeAttributeParser;
import timber.log.Timber;

public class ThemeLineSpacingParser extends ThemeAttributeParser<ThemeLineSpacing> {

    @Override
    public boolean isValueObject(Object value) {
        if (value instanceof Number){
            return true;
        }
        if (value instanceof String) {
            if (((String) value).startsWith("{")) {
                return true;
            }
            try {
                //noinspection ResultOfMethodCallIgnored
                Double.parseDouble(value.toString());
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public ThemeLineSpacing parseObject(Object value) throws AttributeParseException {
        if (value instanceof Number){
            return new ThemeLineSpacing(((Number) value).floatValue(), 0);
        }
        if (value instanceof String) {
            if (((String) value).startsWith("{")) {
                try {
                    JSONObject jsonObject = new JSONObject((String) value);
                    float multiplier = (float) jsonObject.optDouble("multiplier", ThemeLineSpacing.DEFAULT_MULTIPLIER);
                    float extra = (float) jsonObject.optDouble("extra", ThemeLineSpacing.DEFAULT_EXTRA);
                    return new ThemeLineSpacing(multiplier, extra);
                } catch (JSONException e) {
                    Timber.w(e,"Failed to parse lineSpacing");
                }
            }
            try {
                //noinspection ResultOfMethodCallIgnored
                return new ThemeLineSpacing(Float.parseFloat(value.toString()), 0);
            }
            catch (NumberFormatException e) {
                Timber.w(e, "Failed to parse lineSpacing");
            }
        }
        return ThemeLineSpacing.DEFAULT;
    }
}
