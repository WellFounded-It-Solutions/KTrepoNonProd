package se.infomaker.livecontentui.config;

import java.util.Map;

import se.infomaker.livecontentmanager.parser.PropertyObject;

public class ThemeOverlayConfig {
    private String property;
    private Map<String, String> theme;

    public String getOverlayThemeFile(PropertyObject propertyObject) {
        if (propertyObject == null || property == null || theme == null) {
            return null;
        }
        String value = propertyObject.optString(property);
        return theme.get(value);
    }
}