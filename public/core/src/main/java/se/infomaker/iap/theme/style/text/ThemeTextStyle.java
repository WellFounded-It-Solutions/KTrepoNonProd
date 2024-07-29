package se.infomaker.iap.theme.style.text;

import android.text.TextPaint;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.alignment.ThemeAlignment;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.font.ThemeFont;
import se.infomaker.iap.theme.letterspacing.ThemeLetterSpacing;
import se.infomaker.iap.theme.linespacing.ThemeLineSpacing;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.theme.style.decoration.ThemeStrikethrough;
import se.infomaker.iap.theme.transforms.ThemeTransforms;
import se.infomaker.iap.theme.style.decoration.ThemeUnderline;
import se.infomaker.iap.theme.util.UI;

public class ThemeTextStyle {
    public static final ThemeTextStyle DEFAULT = new ThemeTextStyleBuilder()
            .setColor(ThemeColor.BLACK)
            .setSize(ThemeSize.DEFAULT)
            .setFont(ThemeFont.DEFAULT)
            .setLineSpacing(ThemeLineSpacing.DEFAULT)
            .setTransforms(ThemeTransforms.DEFAULT)
            .setAlignment(ThemeAlignment.DEFAULT)
            .setLetterSpacing(ThemeLetterSpacing.DEFAULT)
            .setUnderline(ThemeUnderline.DEFAULT)
            .setStrikethrough(ThemeStrikethrough.DEFAULT)
            .build();

    final ThemeColor color;
    private final String colorReference;
    final ThemeSize size;
    private final String sizeReference;
    final ThemeFont font;
    private final String fontReference;
    final ThemeLineSpacing lineSpacing;
    private final String lineSpacingReference;
    private final ThemeTransforms transforms;
    private final String transformsReference;
    private final ThemeAlignment alignment;
    private final String alignmentReference;
    private final ThemeLetterSpacing letterSpacing;
    private final String letterSpacingReference;
    private final ThemeUnderline underline;
    private final ThemeStrikethrough strikethrough;

    public ThemeTextStyle(ThemeColor color, String colorReference, ThemeSize size, String sizeReference, ThemeFont font, String fontReference, ThemeLineSpacing lineSpacing, String lineSpacingReference, ThemeTransforms transforms, String transformsReference, ThemeAlignment alignment, String alignmentReference, ThemeLetterSpacing letterSpacing, String letterSpacingReference, ThemeUnderline underline, ThemeStrikethrough strikethrough) {
        this.color = color;
        this.colorReference = colorReference;
        this.size = size;
        this.sizeReference = sizeReference;
        this.font = font;
        this.fontReference = fontReference;
        this.lineSpacing = lineSpacing;
        this.lineSpacingReference = lineSpacingReference;
        this.transforms = transforms;
        this.transformsReference = transformsReference;
        this.alignment = alignment;
        this.alignmentReference = alignmentReference;
        this.letterSpacing = letterSpacing;
        this.letterSpacingReference = letterSpacingReference;
        this.underline = underline;
        this.strikethrough = strikethrough;
    }

    public static ThemeTextStyle fromTextView(TextView view) {
        return new ThemeTextStyleBuilder()
                .setColor(new ThemeColor(view.getCurrentTextColor()))
                .setSize(new ThemeSize(UI.px2dp(view.getTextSize())))
                .setFont(new ThemeFont(view.getTypeface()))
                .setLineSpacing(new ThemeLineSpacing(view.getLineSpacingMultiplier(), UI.px2dp(view.getLineSpacingExtra())))
                .setAlignment(new ThemeAlignment(view.getTextAlignment()))
                .setLetterSpacing(new ThemeLetterSpacing(view.getLetterSpacing()))
                .build();
    }

    /**
     * Applies style to the view resolving any references from the theme
     *
     * @param theme to resolve references from
     * @param view  to apply style to
     */
    public void apply(Theme theme, TextView view) {
        (color != null ? color : theme.getColor(colorReference, DEFAULT.color)).apply(view);
        (size != null ? size : theme.getSize(sizeReference, DEFAULT.size)).apply(view);
        (font != null ? font : theme.getFont(fontReference, DEFAULT.font)).apply(view);
        (lineSpacing != null ? lineSpacing : theme.getLineSpacing(lineSpacingReference, DEFAULT.lineSpacing)).apply(view);
        (transforms != null ? transforms : theme.getTransforms(transformsReference, DEFAULT.transforms)).apply(view);
        (alignment != null ? alignment : theme.getAlignment(alignmentReference, DEFAULT.alignment)).apply(view);
        (letterSpacing != null ? letterSpacing : theme.getLetterSpacing(letterSpacingReference, DEFAULT.letterSpacing)).apply(view);
        (underline != null ? underline : ThemeUnderline.DEFAULT).apply(view);
        (strikethrough != null ? strikethrough : ThemeStrikethrough.DEFAULT).apply(view);
    }

    public void paint(Theme theme, TextPaint paint) {
        (color != null ? color : theme.getColor(colorReference, DEFAULT.color)).paint(paint);
        (size != null ? size : theme.getSize(sizeReference, DEFAULT.size)).paint(paint);
        (font != null ? font : theme.getFont(fontReference, DEFAULT.font)).paint(paint);
        (letterSpacing != null ? letterSpacing : theme.getLetterSpacing(letterSpacingReference, DEFAULT.letterSpacing)).paint(paint);
        (underline != null ? underline : ThemeUnderline.DEFAULT).apply(paint);
        (strikethrough != null ? strikethrough : ThemeStrikethrough.DEFAULT).apply(paint);
    }

    public ThemeFont getFont(Theme theme) {
        return theme.getFont(fontReference, font != null ? font : DEFAULT.font);
    }

    public ThemeSize getSize(Theme theme) {
        return theme.getSize(sizeReference, size != null ? size : DEFAULT.size);
    }

    public ThemeAlignment getAlignment(Theme theme/*, ThemeAlignment fallback*/) {
        return theme.getAlignment(alignmentReference, DEFAULT.alignment);
    }

    public ThemeColor getColor(Theme theme) {
        return theme.getColor(colorReference, color != null ? color : DEFAULT.color);
    }

    public ThemeLineSpacing getLineSpacing(Theme theme) {
        return theme.getLineSpacing(lineSpacingReference, ThemeLineSpacing.DEFAULT);
    }

    @NonNull
    public ThemeTransforms getTransforms(Theme theme) {
        return (transforms != null ? transforms : theme.getTransforms(transformsReference, DEFAULT.transforms));
    }

    public ThemeTextStyleBuilder buildUpon() {
        return new ThemeTextStyleBuilder()
                .setColor(color)
                .setColorReference(colorReference)
                .setSize(size)
                .setSizeReference(sizeReference)
                .setFont(font)
                .setFontReference(fontReference)
                .setLineSpacing(lineSpacing)
                .setLineSpacingReference(lineSpacingReference)
                .setTransforms(transforms)
                .setTransformsReference(transformsReference)
                .setAlignment(alignment)
                .setAlignmentReference(alignmentReference)
                .setLetterSpacing(letterSpacing)
                .setLetterSpacingReferenceReference(letterSpacingReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, colorReference, size, sizeReference,
                font, fontReference, lineSpacing, lineSpacingReference,
                transforms, transformsReference, alignment, alignmentReference,
                letterSpacing, letterSpacingReference, underline, strikethrough);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThemeTextStyle)) {
            return false;
        }
        ThemeTextStyle other = (ThemeTextStyle) obj;
        return Objects.equals(color, other.color)
                && Objects.equals(colorReference, other.colorReference)
                && Objects.equals(size, other.size)
                && Objects.equals(sizeReference, other.sizeReference)
                && Objects.equals(font, other.font)
                && Objects.equals(fontReference, other.fontReference)
                && Objects.equals(lineSpacing, other.lineSpacing)
                && Objects.equals(lineSpacingReference, other.lineSpacingReference)
                && Objects.equals(transforms, other.transforms)
                && Objects.equals(transformsReference, other.transformsReference)
                && Objects.equals(alignment, other.alignment)
                && Objects.equals(alignmentReference, other.alignmentReference)
                && Objects.equals(letterSpacing, other.letterSpacing)
                && Objects.equals(letterSpacingReference, other.letterSpacingReference)
                && Objects.equals(underline, other.underline)
                && Objects.equals(strikethrough, other.strikethrough);
    }

    @NonNull
    @Override
    public String toString() {
        return "ThemeTextStyle(color=" + color + ", colorReference=" + colorReference
                + ", size=" + size + ", sizeReference=" + sizeReference
                + ", font=" + font + ", fontReference=" + fontReference
                + ", lineSpacing=" + lineSpacing + ", lineSpacingReference=" + lineSpacingReference
                + ", transforms=" + transforms + ", transformsReference=" + transformsReference
                + ", alignment=" + alignment + ", alignmentReference=" + alignmentReference
                + ", letterSpacing=" + letterSpacing + ", letterSpacingReference=" + letterSpacingReference
                + ", underline=" + underline + ", strikethrough=" + strikethrough
                + ")";
    }
}
