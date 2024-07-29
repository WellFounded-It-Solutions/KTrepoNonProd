package se.infomaker.iap.theme;

import android.view.View;

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
 * Provides theme attributes for keys
 */
public interface Theme {
    /**
     * Provides a color defined for the name or fallback if the color is undefined
     * @param name of color in theme
     * @param fallback to use if color is undefined for name
     * @return ThemeColor for name or fallback if not defined
     */
    ThemeColor getColor(String name, ThemeColor fallback);

    /**
     * Provides a color defined for the first matching name in names or fallback if the color is undefined
     * @param names of colors in theme
     * @param fallback fallback to use if color is undefined for name
     * @return ThemeColor for first matching name in names or fallback if not defined
     */
    ThemeColor getColor(List<String> names, ThemeColor fallback);

    /**
     * Provides a color defined for the first matching name in names or fallback if the color is undefined
     * @param names of colors in theme
     * @param fallback fallback to use if color is undefined for name
     * @return ThemeColor for first matching name in names or fallback if not defined
     */
    ThemeColor getColor(ThemeColor fallback, String... names);

    /**
     * Provides a size defined for the name or fallback if the size
     * is undefined
     * @param name of size in theme
     * @param fallback to use if size is undefined for name
     * @return ThemeSize for name or fallback if not defined
     */
    ThemeSize getSize(String name, ThemeSize fallback);

    /**
     * Provides a size defined for the name or fallback if the size
     * is undefined
     * @param names of sizes in theme
     * @param fallback to use if size is undefined for name
     * @return ThemeSize for first matching name in names or fallback if not defined
     */
    ThemeSize getSize(List<String> names, ThemeSize fallback);

    /**
     * Provides a size defined for the name or fallback if the size
     * is undefined
     * @param names of sizes in theme
     * @param fallback to use if size is undefined for name
     * @return ThemeSize for first matching name in names or fallback if not defined
     */
    ThemeSize getSize(ThemeSize fallback, String... names);

    /**
     * Provides a font defined for the name or fallback if the font
     * is undefined
     * @param name of the font in theme
     * @param fallback to use if font is undefined for name
     * @return ThemeFont for name or fallback if not defined
     */
    ThemeFont getFont(String name, ThemeFont fallback);

    /**
     * Provides a font defined for the name or fallback if the font
     * is undefined
     * @param names list of names, returning the first matching
     * @param fallback to use if font is undefined for name
     * @return ThemeFont for first matching name in names or fallback if not defined
     */
    ThemeFont getFont(List<String> names, ThemeFont fallback);

    /**
     * Provides a font defined for the name or fallback if the font
     * is undefined
     * @param names list of names, returning the first matching
     * @param fallback to use if font is undefined for name
     * @return ThemeFont for first matching name in names or fallback if not defined
     */
    ThemeFont getFont(ThemeFont fallback, String... names);

    /**
     * Provides a linespacing defined for the name or fallback if the linespacing
     * is undefined
     * @param name of the linespacing in theme
     * @param fallback to use if linespacing is undefined for name
     * @return ThemeLineSpacing for name or fallback if not defined
     */
    ThemeLineSpacing getLineSpacing(String name, ThemeLineSpacing fallback);

    /**
     * Provides a linespacing defined for the name or fallback if the linespacing
     * is undefined
     * @param names of the linespaces in theme
     * @param fallback to use if linespacing is undefined for name
     * @return ThemeLineSpacing for first matching name in names or fallback if not defined
     */
    ThemeLineSpacing getLineSpacing(List<String> names, ThemeLineSpacing fallback);

    /**
     * Provides a linespacing defined for the name or fallback if the linespacing
     * is undefined
     * @param names of the linespaces in theme
     * @param fallback to use if linespacing is undefined for name
     * @return ThemeLineSpacing for first matching name in names or fallback if not defined
     */
    ThemeLineSpacing getLineSpacing(ThemeLineSpacing fallback, String... names);

    /**
     * Provides a textStyle defined for the name or fallback if the font
     * is undefined
     * @param name of the font in theme
     * @param fallback to use if font is undefined for name
     * @return ThemeTextStyle for name or fallback if not defined
     */
    ThemeTextStyle getText(String name, ThemeTextStyle fallback);

    /**
     * Provides a textStyle defined for the first matching name in names or fallback if the font
     * is undefined
     * @param names of the fonts in theme
     * @param fallback to use if font is undefined for name
     * @return ThemeTextStyle for first matching name in names or fallback if not defined
     */
    ThemeTextStyle getText(List<String> names, ThemeTextStyle fallback);

    /**
     * Provides a textStyle defined for the first matching name in names or fallback if the font
     * is undefined
     * @param names of the fonts in theme
     * @param fallback to use if font is undefined for name
     * @return ThemeTextStyle for first matching name in names or fallback if not defined
     */
    ThemeTextStyle getText(ThemeTextStyle fallback, String... names);

    /**
     * Provides an image defined for the name or fallback if the image
     * is undefined
     * @param name of the font in theme
     * @param fallback to use if image is undefined for name
     * @return ThemeImage for name or fallback if not defined
     */
    ThemeImage getImage(String name, ThemeImage fallback);

    /**
     * Provides an image defined for the name or fallback if the image
     * is undefined
     * @param names of the font in theme
     * @param fallback to use if image is undefined for names
     * @return ThemeImage for the first matchign name or fallback if not defined
     */
    ThemeImage getImage(List<String> names, ThemeImage fallback);

    /**
     * Provides a image defined for the name or fallback if the image
     * is undefined
     * @param fallback image to use when no image defined in names is found
     * @param names of the font in theme
     * @return ThemeImage for the first matchign name or fallback if not defined
     */
    ThemeImage getImage(ThemeImage fallback, String... names);

    /**
     * Apply theme to all themeable children off view
     * @param view view to apply theme to (and all subviews)
     */
    void apply(View view);

    /**
     * Provides a {@link ThemeTransforms} defined for the name or fallback if the transformer
     * is undefined
     * @param name  of the transformer in the theme
     * @param fallback to use if the font is undefined for name
     * @return {@link ThemeTransforms} for name or fallback if not defined
     */
    ThemeTransforms getTransforms(String name, ThemeTransforms fallback);

    /**
     * Provides a ThemeAlignment defined for the name
     * @param name of alignment in theme
     * @param fallback alignment
     * @return String for name or null
     */
    ThemeAlignment getAlignment(String name, ThemeAlignment fallback);

    /**
     * Provides a {@link ThemeLetterSpacing} defined for the name or fallback if the letterspacing
     * is undefined.
     *
     * @param name of a letterspacing in theme
     * @param fallback to use if the latterspacing is undefined for name
     * @return {@link ThemeLetterSpacing} for name or fallback if not defined
     */
    ThemeLetterSpacing getLetterSpacing(String name, ThemeLetterSpacing fallback);
}
