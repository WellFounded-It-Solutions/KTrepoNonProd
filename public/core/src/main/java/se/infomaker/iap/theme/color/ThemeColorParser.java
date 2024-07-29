package se.infomaker.iap.theme.color;

import android.graphics.Color;

import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.attribute.ThemeAttributeParser;

public class ThemeColorParser extends ThemeAttributeParser<ThemeColor> {

    public static final ThemeColorParser SHARED_INSTANCE = new ThemeColorParser();

    @Override
    public boolean isValueObject(Object value) {
        if (value instanceof String && ((String) value).startsWith("#")) {
            try {
                return parseObject(value) != null;
            } catch (AttributeParseException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public ThemeColor parseObject(Object value) throws AttributeParseException {
        if (!(value instanceof String)) {
            throw new AttributeParseException("Unsupported value: " + value);
        }
        try {
            return new ThemeColor(Color.parseColor((String)value));
        }
        catch (IllegalArgumentException e) {
            throw new AttributeParseException("Could not parse color", e);
        }
    }
}
