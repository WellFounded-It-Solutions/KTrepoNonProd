package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.END_TAG
import se.infomaker.iap.articleview.item.fallback.FallbackItemParser
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.unsupported.UnsupportedItem


class ObjectParser(private val typeParsers: Map<String, ItemParser>) : ItemParser{
    private val fallbackParser = FallbackItemParser()
    override fun parse(parser: XmlPullParser): List<Item> {
        val attributes = parser.getAttributes()
        val type = attributes["type"] ?: ""
        val typeParser = typeParsers[type]
        if (typeParser != null) {
            return typeParser.parse(parser)
        }
        return fallbackParser.parseOrFallback(parser, { type, attributes ->
            listOf(UnsupportedItem(type, attributes))
        })
    }
}