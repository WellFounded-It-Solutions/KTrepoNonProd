package se.infomaker.iap.articleview.transformer.newsml.parser

import android.text.SpannableStringBuilder
import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.element.ElementItem
import java.util.UUID


class ElementParser : ItemParser {

    companion object {
        var ELEMENT_THEME_KEY = "element"
        var DEFAULT_THEME_KEY = "default"
    }

    private val tagExtractors = mutableMapOf<String, SpanExtractor>()
    private val typeParsers = mutableMapOf<String, ElementListParser>()

    init {
        register(AExtractor())
        register(EmExtractor())
        register(StrongExtractor())
        register(InsExtractor())
        register(MarkExtractor())

        typeParsers["x-im/unordered-list"] = UnorderedListParser(tagExtractors)
        typeParsers["x-im/ordered-list"] = OrderedListParser(tagExtractors)
    }

    private fun register(spanExtractor: SpanExtractor) {
        tagExtractors[spanExtractor.tagName] = spanExtractor
    }

    override fun parse(parser: XmlPullParser): List<Item> {
        val attributes = parser.getAttributes()
        val variation = attributes["variation"]
        val type = attributes["type"]

        typeParsers[type]?.let {
            return it.parse(parser)
        }
        val stringBuilder = SpannableStringBuilder()
        var done = false
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    tagExtractors[parser.name]?.start(parser, stringBuilder.length)
                }
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name
                    tagExtractors[tagName]?.end(stringBuilder, stringBuilder.length)

                    if (tagName == "element") {
                        done = true
                    }
                }
                XmlPullParser.TEXT -> stringBuilder.append(parser.text)
            }
        }
        val uuid = attributes["id"] ?: "Detta är trasigt " + UUID.randomUUID().toString()
        return listOf(ElementItem(uuid, listOf(ELEMENT_THEME_KEY, DEFAULT_THEME_KEY).addOptionalThemeKeys(variation, type), attributes, stringBuilder))
    }

    fun extractSingleElement(parser: XmlPullParser, endTag: String, customUUID:String? = null): ElementItem {
        val attributes = parser.getAttributes()
        val stringBuilder = SpannableStringBuilder()
        var done = false
        val tags = mutableListOf<String>()
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "em" -> tags.add("em")
                        "strong" -> tags.add("strong")
                        "a" -> tags.add("a")
                    }
                    tagExtractors[parser.name]?.start(parser, stringBuilder.length)
                }
                XmlPullParser.END_TAG -> {
                    tagExtractors[parser.name]?.end(stringBuilder, stringBuilder.length)
                    if (endTag == parser.name) {
                        done = true
                    }
                }
                XmlPullParser.TEXT -> stringBuilder.append(parser.text)
            }
        }
        val uuid = attributes["id"] ?: customUUID ?: "Detta är trasigt " + UUID.randomUUID().toString()
        tags.add(ELEMENT_THEME_KEY)
        tags.add(DEFAULT_THEME_KEY)
        return ElementItem(uuid, tags, attributes, stringBuilder)
    }
}

fun List<String>.addOptionalThemeKeys(variation: String?, type: String?): List<String> {
    val themeKeys = mutableListOf<String>()
    variation?.let {
        themeKeys.add(it)
    }
    type?.let {
        themeKeys.add(it)
    }
    themeKeys.addAll(this)
    return themeKeys
}