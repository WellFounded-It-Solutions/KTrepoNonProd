package se.infomaker.iap.theme;

import java.util.List;

import se.infomaker.iap.theme.alignment.ThemeAlignment;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.font.ThemeFont;
import se.infomaker.iap.theme.image.ThemeImage;
import se.infomaker.iap.theme.letterspacing.ThemeLetterSpacing;
import se.infomaker.iap.theme.linespacing.ThemeLineSpacing;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;
import se.infomaker.iap.theme.transforms.ThemeTransforms;

/**
 * Empty theme
 */
@SuppressWarnings("WeakerAccess")
public class EmptyTheme extends BaseTheme {
    public static final EmptyTheme INSTANCE = new EmptyTheme();

    public EmptyTheme() {
    }

    @Override
    public ThemeColor getColor(String name, ThemeColor fallback) {
        return fallback;
    }

    @Override
    public ThemeColor getColor(List<String> names, ThemeColor fallback) {
        return fallback;
    }

    @Override
    public ThemeColor getColor(ThemeColor fallback, String... names) {
        return fallback;
    }

    @Override
    public ThemeSize getSize(String name, ThemeSize fallback) {
        return fallback;
    }

    @Override
    public ThemeSize getSize(List<String> names, ThemeSize fallback) {
        return fallback;
    }

    @Override
    public ThemeSize getSize(ThemeSize fallback, String... names) {
        return fallback;
    }

    @Override
    public ThemeFont getFont(String name, ThemeFont fallback) {
        return fallback;
    }

    @Override
    public ThemeFont getFont(List<String> names, ThemeFont fallback) {
        return fallback;
    }

    @Override
    public ThemeFont getFont(ThemeFont fallback, String... names) {
        return fallback;
    }

    @Override
    public ThemeLineSpacing getLineSpacing(String name, ThemeLineSpacing fallback) {
        return fallback;
    }

    @Override
    public ThemeLineSpacing getLineSpacing(List<String> names, ThemeLineSpacing fallback) {
        return fallback;
    }

    @Override
    public ThemeLineSpacing getLineSpacing(ThemeLineSpacing fallback, String... names) {
        return fallback;
    }

    @Override
    public ThemeTextStyle getText(String name, ThemeTextStyle fallback) {
        return fallback;
    }

    @Override
    public ThemeTextStyle getText(List<String> names, ThemeTextStyle fallback) {
        return fallback;
    }

    @Override
    public ThemeTextStyle getText(ThemeTextStyle fallback, String... names) {
        return fallback;
    }

    @Override
    public ThemeImage getImage(String name, ThemeImage fallback) {
        return fallback;
    }

    @Override
    public ThemeImage getImage(List<String> names, ThemeImage fallback) {
        return fallback;
    }

    @Override
    public ThemeImage getImage(ThemeImage fallback, String... names) {
        return fallback;
    }

    @Override
    public ThemeTransforms getTransforms(String name, ThemeTransforms fallback) {
        return fallback;
    }

    @Override
    public ThemeAlignment getAlignment(String name, ThemeAlignment fallback) {
        return fallback;
    }

    @Override
    public ThemeLetterSpacing getLetterSpacing(String name, ThemeLetterSpacing fallback) {
        return fallback;
    }
}
