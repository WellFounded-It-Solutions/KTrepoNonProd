package se.infomaker.iap.theme.view;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.color.ThemeColorParser;

public class ThemeableUtil {
    public static ThemeColor getThemeColor(Theme theme, String color, ThemeColor fallback) {
        ThemeColor touchColor;
        try {
            touchColor = ThemeColorParser.SHARED_INSTANCE.parseObject(color);
        } catch (AttributeParseException e) {
            touchColor = theme.getColor(color, fallback);
        }
        return touchColor;
    }
}
