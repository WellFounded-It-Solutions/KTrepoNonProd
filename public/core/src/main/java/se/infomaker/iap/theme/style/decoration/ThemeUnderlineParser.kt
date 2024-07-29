package se.infomaker.iap.theme.style.decoration

import se.infomaker.iap.theme.attribute.AttributeParseException
import se.infomaker.iap.theme.attribute.ThemeAttributeParser

class ThemeUnderlineParser : ThemeAttributeParser<ThemeUnderline>() {
    override fun isValueObject(value: Any?) = when(value) {
        is String -> true
        else -> false
    }

    @Throws(AttributeParseException::class)
    override fun parseObject(value: Any?) = when(value) {
        is String -> ThemeUnderline(ThemeLineDecoration.from(value))
        else -> ThemeUnderline.DEFAULT
    }
}