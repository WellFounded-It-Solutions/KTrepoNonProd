package se.infomaker.iap.theme.alignment

import se.infomaker.iap.theme.attribute.AttributeParseException
import se.infomaker.iap.theme.attribute.ThemeAttributeParser
import java.util.Locale

class ThemeAlignmentParser : ThemeAttributeParser<ThemeAlignment?>() {

    private val validAlignments = listOf("left", "right", "center")

    override fun isValueObject(value: Any): Boolean {
        return value is String && validAlignments.contains(value)
    }

    @Throws(AttributeParseException::class)
    public override fun parseObject(value: Any): ThemeAlignment {
        if (value !is String) {
            throw AttributeParseException("Unsupported value: $value")
        }
        if(!validAlignments.contains(value)) {
            throw AttributeParseException("Invalid alignment: $value")
        }
        val param = when (value.toUpperCase(Locale.getDefault())) {
            ThemeAlignments.LEFT.name -> ThemeAlignments.LEFT.ordinal
            ThemeAlignments.RIGHT.name -> ThemeAlignments.RIGHT.ordinal
            ThemeAlignments.CENTER.name -> ThemeAlignments.CENTER.ordinal
            else -> ThemeAlignments.INHERIT.ordinal
        }
        return ThemeAlignment(param)
    }
}