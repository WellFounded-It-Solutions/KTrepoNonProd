package se.infomaker.iap.theme.style.text;

import org.json.JSONException;
import org.json.JSONObject;

import se.infomaker.iap.theme.LayeredThemeBuilder;
import se.infomaker.iap.theme.alignment.ThemeAlignmentParser;
import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.attribute.ThemeAttributeParser;
import se.infomaker.iap.theme.color.ThemeColorParser;
import se.infomaker.iap.theme.font.ThemeFontParser;
import se.infomaker.iap.theme.letterspacing.ThemeLetterSpacingParser;
import se.infomaker.iap.theme.linespacing.ThemeLineSpacingParser;
import se.infomaker.iap.theme.size.ThemeSizeParser;
import se.infomaker.iap.theme.style.decoration.ThemeStrikethroughParser;
import se.infomaker.iap.theme.style.decoration.ThemeUnderlineParser;
import se.infomaker.iap.theme.transforms.ThemeTransformsParser;
import timber.log.Timber;

public class ThemeTextStyleParser extends ThemeAttributeParser<ThemeTextStyle> {
    private final ThemeColorParser colorParser;
    private final ThemeSizeParser sizeParser;
    private final ThemeFontParser fontParser;
    private final ThemeLineSpacingParser lineSpacingParser;
    private final ThemeTransformsParser transformsParser;
    private final ThemeAlignmentParser alignmentParser;
    private final ThemeLetterSpacingParser letterSpacingParser;
    private final ThemeUnderlineParser underlineParser;
    private final ThemeStrikethroughParser strikethroughParser;

    public ThemeTextStyleParser(ThemeColorParser colorParser, ThemeSizeParser sizeParser, ThemeFontParser fontParser, ThemeLineSpacingParser lineSpacingParser, ThemeTransformsParser transformsParser, ThemeAlignmentParser alignmentParser, ThemeLetterSpacingParser letterSpacingParser, ThemeUnderlineParser underlineParser, ThemeStrikethroughParser strikethroughParser) {
        this.colorParser = colorParser;
        this.sizeParser = sizeParser;
        this.fontParser = fontParser;
        this.lineSpacingParser = lineSpacingParser;
        this.transformsParser = transformsParser;
        this.alignmentParser = alignmentParser;
        this.letterSpacingParser = letterSpacingParser;
        this.underlineParser = underlineParser;
        this.strikethroughParser = strikethroughParser;
    }

    @Override
    public boolean isValueObject(Object value) {
        return (value instanceof String && ((String) value).startsWith("{"));
    }

    @Override
    public ThemeTextStyle parseObject(Object value) throws AttributeParseException {
        ThemeTextStyleBuilder builder = new ThemeTextStyleBuilder();
        JSONObject definition;
        try {
            definition = new JSONObject((String) value);
        } catch (JSONException e) {
            throw new AttributeParseException("Could not parse", e);
        }
        if (definition.has(LayeredThemeBuilder.COLOR_KEY)) {
            if (colorParser.isValueObject(definition.optString(LayeredThemeBuilder.COLOR_KEY))){
                try {
                    builder.setColor(colorParser.parseObject(definition.optString(LayeredThemeBuilder.COLOR_KEY, null)));
                }
                catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse color");
                }
            }
            else {
                builder.setColorReference(definition.optString(LayeredThemeBuilder.COLOR_KEY));
            }
        }

        if (definition.has(LayeredThemeBuilder.SIZE_KEY)) {
            if (sizeParser.isValueObject(definition.optString(LayeredThemeBuilder.SIZE_KEY))){
                try {
                    builder.setSize(sizeParser.parseObject(definition.optString(LayeredThemeBuilder.SIZE_KEY, null)));
                }
                catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse size");
                }
            }
            else {
                builder.setSizeReference(definition.optString(LayeredThemeBuilder.SIZE_KEY));
            }
        }

        if (definition.has(LayeredThemeBuilder.FONT_KEY)) {
            if (fontParser.isValueObject(definition.optString(LayeredThemeBuilder.FONT_KEY))){
                try {
                    builder.setFont(fontParser.parseObject(definition.optString(LayeredThemeBuilder.FONT_KEY, null)));
                }
                catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse font");
                }
            }
            else {
                builder.setFontReference(definition.optString(LayeredThemeBuilder.FONT_KEY));
            }
        }
        if (definition.has(LayeredThemeBuilder.LINE_SPACING_KEY)) {
            if (lineSpacingParser.isValueObject(definition.optString(LayeredThemeBuilder.LINE_SPACING_KEY))){
                try {
                    builder.setLineSpacing(lineSpacingParser.parseObject(definition.optString(LayeredThemeBuilder.LINE_SPACING_KEY, null)));
                }
                catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse line spacing");
                }
            }
            else {
                builder.setLineSpacingReference(definition.optString(LayeredThemeBuilder.LINE_SPACING_KEY));
            }
        }

        if(definition.has(LayeredThemeBuilder.TRANSFORMS_KEY)) {
            if (transformsParser.isValueObject(definition.optString(LayeredThemeBuilder.TRANSFORMS_KEY))) {
                try {
                    builder.setTransforms(transformsParser.parseObject(definition.optString(LayeredThemeBuilder.TRANSFORMS_KEY, null)));
                } catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse transforms");
                }
            } else {
                builder.setTransformsReference(definition.optString(LayeredThemeBuilder.TRANSFORMS_KEY));
            }
        }

        if (definition.has(LayeredThemeBuilder.ALIGNMENT_KEY)) {
            if (alignmentParser.isValueObject(definition.optString(LayeredThemeBuilder.ALIGNMENT_KEY))) {
                try {
                    builder.setAlignment(alignmentParser.parseObject(definition.optString(LayeredThemeBuilder.ALIGNMENT_KEY, null)));
                } catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse alignment");
                }
            }else {
                builder.setAlignmentReference(definition.optString(LayeredThemeBuilder.ALIGNMENT_KEY));
            }
        }

        if (definition.has(LayeredThemeBuilder.LETTER_SPACING_KEY)) {
            if (letterSpacingParser.isValueObject(definition.optString(LayeredThemeBuilder.LETTER_SPACING_KEY))) {
                try {
                    builder.setLetterSpacing(letterSpacingParser.parseObject(definition.optString(LayeredThemeBuilder.LETTER_SPACING_KEY, null)));
                } catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse letterspacing");
                }
            }
            else {
                builder.setLetterSpacingReferenceReference(definition.optString(LayeredThemeBuilder.LETTER_SPACING_KEY));
            }
        }

        if (definition.has(LayeredThemeBuilder.UNDERLINE_KEY)) {
            if (underlineParser.isValueObject(definition.optString(LayeredThemeBuilder.UNDERLINE_KEY))) {
                try {
                    builder.setUnderline(underlineParser.parseObject(definition.optString(LayeredThemeBuilder.UNDERLINE_KEY, null)));
                } catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse underline");
                }
            }
        }

        if (definition.has(LayeredThemeBuilder.STRIKETHROUGH_KEY)) {
            if (strikethroughParser.isValueObject(definition.optString(LayeredThemeBuilder.STRIKETHROUGH_KEY))) {
                try {
                    builder.setStrikethrough(strikethroughParser.parseObject(definition.optString(LayeredThemeBuilder.STRIKETHROUGH_KEY, null)));
                } catch (AttributeParseException e) {
                    Timber.w(e, "Could not parse underline");
                }
            }
        }

        return builder.build();
    }
}
