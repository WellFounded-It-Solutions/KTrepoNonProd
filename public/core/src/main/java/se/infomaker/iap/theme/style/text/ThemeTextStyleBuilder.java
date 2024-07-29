package se.infomaker.iap.theme.style.text;

import se.infomaker.iap.theme.alignment.ThemeAlignment;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.font.ThemeFont;
import se.infomaker.iap.theme.letterspacing.ThemeLetterSpacing;
import se.infomaker.iap.theme.linespacing.ThemeLineSpacing;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.theme.style.decoration.ThemeStrikethrough;
import se.infomaker.iap.theme.transforms.ThemeTransforms;
import se.infomaker.iap.theme.style.decoration.ThemeUnderline;

@SuppressWarnings("UnusedReturnValue")
public class ThemeTextStyleBuilder {
    private ThemeColor color;
    private String colorReference;
    private ThemeSize size;
    private String sizeReference;
    private ThemeFont font;
    private String fontReference;
    private ThemeLineSpacing lineSpacing;
    private String lineSpacingReference;
    private ThemeTransforms transforms;
    private String transformsReference;
    private ThemeAlignment alignment;
    private String alignmentReference;
    private ThemeLetterSpacing letterSpacing;
    private String letterSpacingReference;
    private ThemeUnderline underline;
    private ThemeStrikethrough strikethrough;

    public ThemeTextStyleBuilder setColor(ThemeColor color) {
        this.color = color;
        return this;
    }

    public ThemeTextStyleBuilder setColorReference(String colorReference) {
        this.colorReference = colorReference;
        return this;
    }

    public ThemeTextStyleBuilder setSize(ThemeSize size) {
        this.size = size;
        return this;
    }

    public ThemeTextStyleBuilder setSizeReference(String sizeReference) {
        this.sizeReference = sizeReference;
        return this;
    }

    public ThemeTextStyleBuilder setFont(ThemeFont font) {
        this.font = font;
        return this;
    }

    public ThemeTextStyleBuilder setFontReference(String fontReference) {
        this.fontReference = fontReference;
        return this;
    }

    public ThemeTextStyleBuilder setLineSpacing(ThemeLineSpacing lineSpacing) {
        this.lineSpacing = lineSpacing;
        return this;
    }

    public ThemeTextStyleBuilder setLineSpacingReference(String lineSpacingReference) {
        this.lineSpacingReference = lineSpacingReference;
        return this;
    }

    public ThemeTextStyleBuilder setTransforms(ThemeTransforms transforms) {
        this.transforms = transforms;
        return this;
    }

    public ThemeTextStyleBuilder setTransformsReference(String transformsReference) {
        this.transformsReference = transformsReference;
        return this;
    }

    public ThemeTextStyleBuilder setAlignment(ThemeAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public ThemeTextStyleBuilder setAlignmentReference(String alignmentReference) {
        this.alignmentReference = alignmentReference;
        return this;
    }

    public ThemeTextStyleBuilder setLetterSpacing(ThemeLetterSpacing letterSpacing) {
        this.letterSpacing = letterSpacing;
        return this;
    }

    public ThemeTextStyleBuilder setLetterSpacingReferenceReference(String letterSpacingReference) {
        this.letterSpacingReference = letterSpacingReference;
        return this;
    }

    public ThemeTextStyleBuilder setUnderline(ThemeUnderline underline) {
        this.underline = underline;
        return this;
    }

    public ThemeTextStyleBuilder setStrikethrough(ThemeStrikethrough strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public ThemeTextStyle build() {
        if (color == null && colorReference == null) {
            color = ThemeTextStyle.DEFAULT.color;
        }
        if (size == null && sizeReference == null) {
            size = ThemeTextStyle.DEFAULT.size;
        }
        if (font == null && fontReference == null) {
            font = ThemeTextStyle.DEFAULT.font;
        }
        if (lineSpacing == null && lineSpacingReference == null) {
            lineSpacing = ThemeLineSpacing.DEFAULT;
        }
        if(transforms == null && transformsReference == null) {
            transforms = ThemeTransforms.DEFAULT;
        }
        if (alignment == null && alignmentReference == null){
            alignment = ThemeAlignment.DEFAULT;
        }
        if (letterSpacing == null && letterSpacingReference == null) {
            letterSpacing = ThemeLetterSpacing.DEFAULT;
        }
        if (underline == null) {
            underline = ThemeUnderline.DEFAULT;
        }
        if (strikethrough == null) {
            strikethrough = ThemeStrikethrough.DEFAULT;
        }
        return new ThemeTextStyle(
                color, colorReference, size, sizeReference, font, fontReference, lineSpacing,
                lineSpacingReference, transforms, transformsReference, alignment, alignmentReference,
                letterSpacing, letterSpacingReference, underline, strikethrough
        );
    }
}