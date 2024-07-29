package se.infomaker.iap.theme.font;

import se.infomaker.iap.theme.ThemeException;
import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.attribute.ThemeAttributeParser;

public class ThemeFontParser extends ThemeAttributeParser<ThemeFont> {

    private final FontLoader fontLoader;

    public ThemeFontParser(FontLoader fontLoader) {
        this.fontLoader = fontLoader;
    }

    @Override
    public boolean isValueObject(Object value) {
        if (value instanceof String) {
            try {
                return fontLoader.getTypeFace((String) value) != null;
            } catch (ThemeException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public ThemeFont parseObject(Object value) throws AttributeParseException {
        try {
            return new ThemeFont(fontLoader.getTypeFace((String)value));
        } catch (ThemeException e) {
            throw new AttributeParseException("Failed to parse font", e);
        }
    }
}
