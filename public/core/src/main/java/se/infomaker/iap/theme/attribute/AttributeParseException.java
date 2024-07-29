package se.infomaker.iap.theme.attribute;

import se.infomaker.iap.theme.ThemeException;

@SuppressWarnings("SameParameterValue")
public class AttributeParseException extends ThemeException {
    public AttributeParseException() {
    }

    public AttributeParseException(String message) {
        super(message);
    }

    public AttributeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
