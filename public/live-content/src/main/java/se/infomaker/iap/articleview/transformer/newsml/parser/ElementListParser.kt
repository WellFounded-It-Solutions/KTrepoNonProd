package se.infomaker.iap.articleview.transformer.newsml.parser

import android.text.SpannableStringBuilder
import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.element.ElementListItem
import java.util.UUID

abstract class ElementListParser(private val spanExtractors: MutableMap<String, SpanExtractor>) : ItemParser {

    companion object {
        var ELEMENT_LIST_THEME_KEY = "elementList"
        var ELEMENT_THEME_KEY = "element"
        var DEFAULT_THEME_KEY = "default"

        var ELEMENT_LIST_PREFIX_THEME_KEY = "elementListPrefix"
    }

    abstract val primaryThemeKey: String
    abstract val primaryIndicatorThemeKey: String
    abstract val listType: ElementListItem.ListType

    override fun parse(parser: XmlPullParser): List<Item> {

        val attributes = parser.getAttributes()
        val listItems = mutableListOf<ElementItem>()
        val variation = attributes["variation"]

        var stringBuilder = SpannableStringBuilder()
        var done = false
        while (!done) {

            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    spanExtractors[parser.name]?.start(parser, stringBuilder.length)
                }
                XmlPullParser.END_TAG -> {
                    spanExtractors[parser.name]?.end(stringBuilder, stringBuilder.length)?.let {
                        stringBuilder = it
                    }

                    when (parser.name) {
                        "element" -> done = true
                        "list-item" -> {
                            listItems.add(ElementItem("contained", emptyList(), emptyMap(), stringBuilder))
                            stringBuilder = SpannableStringBuilder()
                        }
                    }
                }
                XmlPullParser.TEXT -> stringBuilder.append(parser.text)
            }
        }
        val uuid = attributes["id"] ?: UUID.randomUUID().toString()
        val themeKeys = listOf(ELEMENT_LIST_THEME_KEY, ELEMENT_THEME_KEY, DEFAULT_THEME_KEY).addOptionalThemeKeys(variation, primaryThemeKey)
        return listOf(ElementListItem(
            uuid,
            themeKeys,
            listOf(primaryIndicatorThemeKey, ELEMENT_LIST_PREFIX_THEME_KEY) + themeKeys,
            attributes,
            listItems,
            listType
        ))
    }
}

class OrderedListParser(spanExtractors: MutableMap<String, SpanExtractor>) : ElementListParser(spanExtractors) {

    override val primaryThemeKey = "orderedElementList"
    override val primaryIndicatorThemeKey = "orderedElementListPrefix"
    override val listType = ElementListItem.ListType.ORDERED
}

class UnorderedListParser(spanExtractors: MutableMap<String, SpanExtractor>) : ElementListParser(spanExtractors) {

    override val primaryThemeKey = "unorderedElementList"
    override val primaryIndicatorThemeKey = "unorderedElementListPrefix"
    override val listType = ElementListItem.ListType.UNORDERED
}