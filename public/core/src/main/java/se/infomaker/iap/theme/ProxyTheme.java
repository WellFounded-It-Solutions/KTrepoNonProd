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

public abstract class ProxyTheme extends BaseTheme {

    private final Theme theme;

    public ProxyTheme(Theme theme) {
        this.theme = theme;
    }

    @Override
    public ThemeColor getColor(String name, ThemeColor fallback) {
        return theme.getColor(name, fallback);
    }

    @Override
    public ThemeColor getColor(List<String> names, ThemeColor fallback) {
        return theme.getColor(names, fallback);
    }

    @Override
    public ThemeColor getColor(ThemeColor fallback, String... names) {
        return theme.getColor(fallback, names);
    }

    @Override
    public ThemeSize getSize(String name, ThemeSize fallback) {
        return theme.getSize(name, fallback);
    }

    @Override
    public ThemeSize getSize(List<String> names, ThemeSize fallback) {
        return theme.getSize(names, fallback);
    }

    @Override
    public ThemeSize getSize(ThemeSize fallback, String... names) {
        return theme.getSize(fallback, names);
    }

    @Override
    public ThemeFont getFont(String name, ThemeFont fallback) {
        return theme.getFont(name, fallback);
    }

    @Override
    public ThemeFont getFont(List<String> names, ThemeFont fallback) {
        return theme.getFont(names, fallback);
    }

    @Override
    public ThemeFont getFont(ThemeFont fallback, String... names) {
        return theme.getFont(fallback, names);
    }

    @Override
    public ThemeLineSpacing getLineSpacing(String name, ThemeLineSpacing fallback) {
        return theme.getLineSpacing(name, fallback);
    }

    @Override
    public ThemeLineSpacing getLineSpacing(List<String> names, ThemeLineSpacing fallback) {
        return theme.getLineSpacing(names, fallback);
    }

    @Override
    public ThemeLineSpacing getLineSpacing(ThemeLineSpacing fallback, String... names) {
        return theme.getLineSpacing(fallback, names);
    }

    @Override
    public ThemeTextStyle getText(String name, ThemeTextStyle fallback) {
        return theme.getText(name, fallback);
    }

    @Override
    public ThemeTextStyle getText(List<String> names, ThemeTextStyle fallback) {
        return theme.getText(names, fallback);
    }

    @Override
    public ThemeTextStyle getText(ThemeTextStyle fallback, String... names) {
        return theme.getText(fallback, names);
    }

    @Override
    public ThemeImage getImage(String name, ThemeImage fallback) {
        return theme.getImage(name, fallback);
    }

    @Override
    public ThemeImage getImage(List<String> names, ThemeImage fallback) {
        return theme.getImage(names, fallback);
    }

    @Override
    public ThemeImage getImage(ThemeImage fallback, String... names) {
        return theme.getImage(fallback, names);
    }

    @Override
    public ThemeTransforms getTransforms(String name, ThemeTransforms fallback) {
        return theme.getTransforms(name, fallback);
    }

    @Override
    public ThemeAlignment getAlignment(String name, ThemeAlignment fallback) {
        return theme.getAlignment(name, fallback);
    }

    @Override
    public ThemeLetterSpacing getLetterSpacing(String name, ThemeLetterSpacing fallback) {
        return theme.getLetterSpacing(name, fallback);
    }
}
