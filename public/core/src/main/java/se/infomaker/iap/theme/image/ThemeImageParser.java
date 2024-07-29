package se.infomaker.iap.theme.image;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.attribute.ThemeAttributeParser;

public class ThemeImageParser extends ThemeAttributeParser<ThemeImage> {
    private final ResourceManager resourceManager;

    public ThemeImageParser(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public boolean isValueObject(Object value) {
        if (value instanceof  String) {
            int identifier = resourceManager.getDrawableIdentifier((String) value);
            return identifier != 0;
        }
        return false;
    }

    @Override
    public ThemeImage parseObject(Object value) throws AttributeParseException {
        int identifier = resourceManager.getDrawableIdentifier((String) value);
        if (identifier != 0) {
            return new ThemeImage(identifier);
        }
        throw new AttributeParseException("Images could not be parsed");
    }
}