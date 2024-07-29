package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.decorator.MarginDecorator
import se.infomaker.iap.articleview.item.decorator.PaddingDecorator
import se.infomaker.iap.articleview.item.embed.HtmlEmbedItem
import java.util.*

class HtmlEmbedParser : ItemParser {

    override fun parse(parser: XmlPullParser): List<HtmlEmbedItem> {

        val builder = HtmlEmbedItem.builder {
            type = parser.getAttributes()["type"] ?: type
            uuid = parser.getAttributes()["uuid"] ?: UUID.randomUUID().toString()
        }

        var done = false
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "text" -> {
                            builder.html = parser.nextText()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "object") {
                        done = true
                    }
                }
            }
        }
        
        val item = builder.build()

        item.decorators.add(PaddingDecorator(
            left = listOf("htmlEmbedPaddingLeft", "htmlEmbedPaddingHorizontal", "defaultPaddingLeft", "defaultPaddingHorizontal"),
            top = listOf("htmlEmbedPaddingTop", "htmlEmbedPaddingVertical", "defaultPaddingTop", "defaultPaddingVertical"),
            right = listOf("htmlEmbedPaddingRight", "htmlEmbedPaddingHorizontal", "defaultPaddingRight", "defaultPaddingHorizontal"),
            bottom = listOf("htmlEmbedPaddingBottom", "htmlEmbedPaddingVertical", "defaultPaddingBottom", "defaultPaddingVertical"),
        ))

        item.decorators.add(MarginDecorator(
            left = listOf("htmlEmbedMarginLeft", "htmlEmbedMarginHorizontal", "defaultMarginLeft", "defaultMarginHorizontal"),
            top = listOf("htmlEmbedMarginTop", "htmlEmbedMarginVertical", "defaultMarginTop", "defaultMarginVertical"),
            right = listOf("htmlEmbedMarginRight", "htmlEmbedMarginHorizontal", "defaultMarginRight", "defaultMarginHorizontal"),
            bottom = listOf("htmlEmbedMarginBottom", "htmlEmbedMarginVertical", "defaultMarginBottom", "defaultMarginVertical"),
        ))
        
        return listOf(item)
    }
}