package se.infomaker.iap.articleview.item.fallback

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.decorator.MarginDecorator
import se.infomaker.iap.articleview.ktx.suffixItems
import se.infomaker.iap.articleview.transformer.newsml.parser.forwardTo
import se.infomaker.iap.articleview.transformer.newsml.parser.getAttributes
import java.util.UUID

class FallbackItemParser {
    private val linkTypeParsers = mutableMapOf<String, FallbackLinkParser>()

    init {
        linkTypeParsers["text/html"] = LinkDataAttributeParser()
        linkTypeParsers["image/jpg"] = LinkDataAttributeParser()
    }

    fun parseOrFallback(parser: XmlPullParser, fallback: (String, Map<String, String>) -> List<Item>): List<Item> {
        val attributes = parser.getAttributes()

        var done = false
        val links = mutableListOf<Link>()
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "link" -> {
                            val linkAttributes = parser.getAttributes()
                            var added = false
                            if (linkAttributes["rel"] == "alternate" && linkAttributes["type"] != null) {
                                linkAttributes["type"]?.let { type ->
                                    linkTypeParsers[type]?.let { linkParser ->
                                        links.add(linkParser.parse(parser))
                                        added = true
                                    }
                                }
                            }
                            if (!added) {
                                parser.forwardTo(XmlPullParser.END_TAG, "link")
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "object" -> done = true
                    }
                }
            }
        }
        if (links.size > 0) {
            val horizontalMarginKeys = THEME_SIZE_KEYS.suffixItems("MarginHorizontal")
            val leftMarginKeys = THEME_SIZE_KEYS.suffixItems("MarginLeft").zip(horizontalMarginKeys).flatMap { listOf(it.first, it.second) }
            val rightMarginKeys = THEME_SIZE_KEYS.suffixItems("MarginRight").zip(horizontalMarginKeys).flatMap { listOf(it.first, it.second) }

            val verticalMarginKeys = THEME_SIZE_KEYS.suffixItems("MarginVertical")
            val topMarginKeys = THEME_SIZE_KEYS.suffixItems("MarginTop").zip(verticalMarginKeys).flatMap { listOf(it.first, it.second) }
            val bottomMarginKeys = THEME_SIZE_KEYS.suffixItems("MarginBottom").zip(verticalMarginKeys).flatMap { listOf(it.first, it.second) }
            return listOf(FallbackItem(attributes, links).apply {
                decorators.add(
                    MarginDecorator(
                        left = leftMarginKeys,
                        top = topMarginKeys,
                        right = rightMarginKeys,
                        bottom = bottomMarginKeys
                    )
                )
            })
        }

        return fallback.invoke(attributes["type"] ?: "unknown", attributes)
    }

    companion object {
        private val THEME_SIZE_KEYS = listOf("fallback", "default")
    }
}

class LinkDataAttributeParser : FallbackLinkParser {
    override fun parse(parser: XmlPullParser): Link {

        var done = false
        var key = ""
        val linkAttributes = mutableMapOf<String, String>()
        linkAttributes.putAll(parser.getAttributes())
        val stringBuilder = StringBuilder()
        var inData = false
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "data" -> {
                            inData = true
                        }
                        else -> {
                            if (inData) {
                                key = parser.name
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "data" -> {
                        }
                        "link" -> done = true
                        key -> {
                            linkAttributes.put(key, stringBuilder.toString())
                            stringBuilder.setLength(0)
                            key = ""
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (key.isNotEmpty()) {
                        stringBuilder.append(parser.text)
                    }
                }
            }
        }
        return Link(linkAttributes)
    }

}

interface FallbackLinkParser {
    fun parse(parser: XmlPullParser): Link
}

data class FallbackItem(val attributes: Map<String, String>, val links: List<Link>) : Item(attributes["id"] ?: "generated-" + UUID.randomUUID().toString()) {
    override var typeIdentifier : Any = FallbackItem::class.java
    var template: String? = null
    override val matchingQuery: Map<String, String> = mapOf()
    override val selectorType: String = "fallback"

    val allAttributes: Map<String, String> = let {
        val all = mutableMapOf<String, String>()
        links.forEach {
            all.putAll(it.attributes)
        }
        all.putAll(attributes)
        return@let all
    }

    val type: String
        get() = attributes["type"] ?: "unknown"
    val imageUrl: String?
        get() = links.filter { it.type.startsWith("image") }.map { it.url }.firstOrNull()
    val imageHeight: Int?
        get() = links.filter { it.type.startsWith("image") }.map { it.height }.firstOrNull()
    val imageWidth: Int?
        get() = links.filter { it.type.startsWith("image") }.map { it.width }.firstOrNull()
    val webUrl: String?
        get() = links.filter { it.type.startsWith("text/html") }.map { it.url }.firstOrNull()
    val title: String?
        get() = links.filter { it.title != null }.map { it.title }.firstOrNull()
}

data class Link(val attributes: Map<String, String>) {
    val type: String
        get() = attributes["type"] ?: "unknown"

    val url: String?
        get() = attributes["url"]

    val title: String?
        get() = attributes["title"]

    val height: Int?
        get() = attributes["height"]?.toInt()

    val width: Int?
        get() = attributes["width"]?.toInt()
}
