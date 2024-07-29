package se.infomaker.iap.theme.style.decoration

import se.infomaker.iap.theme.attribute.AttributeParseException
import se.infomaker.iap.theme.attribute.ThemeAttributeParser

class ThemeStrikethroughParser : ThemeAttributeParser<ThemeStrikethrough>() {
    override fun isValueObject(value: Any?) = when(value) {
        is String -> true
        else -> false
    }

    @Throws(AttributeParseException::class)
    override fun parseObject(value: Any?) = when(value) {
        is String -> ThemeStrikethrough(ThemeLineDecoration.from(value))
        else -> ThemeStrikethrough.DEFAULT
    }
}