package se.infomaker.iap.theme.letterspacing

import se.infomaker.iap.theme.attribute.AttributeParseException
import se.infomaker.iap.theme.attribute.ThemeAttributeParser

class ThemeLetterSpacingParser : ThemeAttributeParser<ThemeLetterSpacing>() {

    override fun isValueObject(value: Any?) = when(value) {
        is Number -> true
        is String -> value.toFloatOrNull()?.let { true } ?: false
        else -> false
    }

    @Throws(AttributeParseException::class)
    public override fun parseObject(value: Any?) = when(value) {
        is Number -> ThemeLetterSpacing(value.toFloat())
        is String -> ThemeLetterSpacing(value.toFloat())
        else -> ThemeLetterSpacing.DEFAULT
    }
}