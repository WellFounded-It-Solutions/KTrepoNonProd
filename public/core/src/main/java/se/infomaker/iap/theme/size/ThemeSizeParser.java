package se.infomaker.iap.theme.size;

import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.attribute.ThemeAttributeParser;

public class ThemeSizeParser extends ThemeAttributeParser<ThemeSize> {
    @Override
    public boolean isValueObject(Object value) {
        if (value instanceof Number){
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

    @Override
    public ThemeSize parseObject(Object value) throws AttributeParseException {
        float size;
        if (value instanceof Number) {
            size = ((Number) value).floatValue();
        }
        else {
            try {
                size = Float.valueOf(value.toString());
            }
            catch (NumberFormatException e) {
                throw new AttributeParseException("Failed to parse size " + value, e);
            }
        }
        return new ThemeSize(size);
    }
}
