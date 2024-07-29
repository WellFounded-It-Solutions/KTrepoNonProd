package se.infomaker.iap.theme;

import org.json.JSONObject;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.alignment.ThemeAlignment;
import se.infomaker.iap.theme.alignment.ThemeAlignmentParser;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.color.ThemeColorParser;
import se.infomaker.iap.theme.font.FontLoader;
import se.infomaker.iap.theme.font.ThemeFont;
import se.infomaker.iap.theme.font.ThemeFontParser;
import se.infomaker.iap.theme.image.ThemeImage;
import se.infomaker.iap.theme.image.ThemeImageParser;
import se.infomaker.iap.theme.letterspacing.ThemeLetterSpacing;
import se.infomaker.iap.theme.letterspacing.ThemeLetterSpacingParser;
import se.infomaker.iap.theme.linespacing.ThemeLineSpacing;
import se.infomaker.iap.theme.linespacing.ThemeLineSpacingParser;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.theme.size.ThemeSizeParser;
import se.infomaker.iap.theme.style.decoration.ThemeStrikethroughParser;
import se.infomaker.iap.theme.style.decoration.ThemeUnderlineParser;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;
import se.infomaker.iap.theme.style.text.ThemeTextStyleParser;
import se.infomaker.iap.theme.transforms.ThemeTransforms;
import se.infomaker.iap.theme.transforms.ThemeTransformsParser;

/**
 * Builds a layered theme, optionally on top of another theme
 */
public class LayeredThemeBuilder {

    private static final String ANDROID_OVERRIDES_KEY = "android";
    public static final String COLOR_KEY = "color";
    public static final String LINE_SPACING_KEY = "linespacing";
    public static final String SIZE_KEY = "size";
    public static final String FONT_KEY = "font";
    public static final String TRANSFORMS_KEY = "transforms";
    private static final String TEXT_KEY = "text";
    private static final String IMAGE_KEY = "image";
    public static final String ALIGNMENT_KEY = "alignment";
    public static final String LETTER_SPACING_KEY = "letterspacing";
    public static final String UNDERLINE_KEY = "underline";
    public static final String STRIKETHROUGH_KEY = "strikethrough";

    private LayeredTheme parent;
    private JSONObject definition;

    public LayeredThemeBuilder setParent(LayeredTheme parent) {
        this.parent = parent;
        return this;
    }

    public LayeredThemeBuilder setDefinition(JSONObject definition) {
        this.definition = definition;
        return this;
    }

    public LayeredTheme build(ResourceManager resourceManager, FontLoader fontLoader) {
        if (definition == null) {
            if (parent != null) {
                return parent;
            }
            definition = new JSONObject();
        }
        ThemeColorParser colorParser = new ThemeColorParser();
        Resolver<ThemeColor> colorResolver = colorParser.parse(definition.optJSONObject(COLOR_KEY)).createResolver();

        ThemeSizeParser sizeParser = new ThemeSizeParser();
        Resolver<ThemeSize> sizeResolver = sizeParser.parse(definition.optJSONObject(SIZE_KEY)).createResolver();

        ThemeFontParser fontParser = new ThemeFontParser(fontLoader);
        Resolver<ThemeFont> fontResolver = fontParser.parse(definition.optJSONObject(FONT_KEY)).createResolver();

        ThemeLineSpacingParser lineSpacingParser = new ThemeLineSpacingParser();
        Resolver<ThemeLineSpacing> lineSpacingResolver = lineSpacingParser.parse(definition.optJSONObject(LINE_SPACING_KEY)).createResolver();

        ThemeTransformsParser transformsParser = new ThemeTransformsParser();
        Resolver<ThemeTransforms> transformsResolver = transformsParser.parse(definition.optJSONObject(TRANSFORMS_KEY)).createResolver();

        ThemeAlignmentParser alignmentParser = new ThemeAlignmentParser();
        Resolver<ThemeAlignment> alignmentResolver = alignmentParser.parse(definition.optJSONObject(ALIGNMENT_KEY)).createResolver();

        ThemeLetterSpacingParser letterSpacingParser = new ThemeLetterSpacingParser();
        Resolver<ThemeLetterSpacing> letterSpacingResolver = letterSpacingParser.parse(definition.optJSONObject(LETTER_SPACING_KEY)).createResolver();

        ThemeUnderlineParser underlineParser = new ThemeUnderlineParser();
        ThemeStrikethroughParser strikethroughParser = new ThemeStrikethroughParser();

        ThemeTextStyleParser textStyleParser = new ThemeTextStyleParser(colorParser, sizeParser, fontParser, lineSpacingParser, transformsParser, alignmentParser, letterSpacingParser, underlineParser, strikethroughParser);
        Resolver<ThemeTextStyle> textStyleResolver = textStyleParser.parse(definition.optJSONObject(TEXT_KEY)).createResolver();

        Resolver<ThemeImage> imageResolver = new ThemeImageParser(resourceManager).parse(definition.optJSONObject(IMAGE_KEY)).createResolver();

        return new LayeredTheme(parent, colorResolver, sizeResolver, fontResolver, textStyleResolver, imageResolver, lineSpacingResolver, transformsResolver, alignmentResolver, letterSpacingResolver);
    }
}
