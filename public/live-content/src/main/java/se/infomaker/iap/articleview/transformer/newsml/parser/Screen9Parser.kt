package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.screen9.Screen9Item
import java.lang.StringBuilder

class Screen9Parser : ItemParser {

    override fun parse(parser: XmlPullParser): List<Screen9Item> {

        val items = mutableListOf<Screen9Item>()
        var done = false
        val attributes = parser.getAttributes()
        var data = emptyMap<String, String>()
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "data" -> {
                            data = parseData(parser)
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
        items.add(Screen9Item(attributes, data))
        return items
    }

    private fun parseData(parser: XmlPullParser): Map<String, String> {
        var done = false
        var data = mutableMapOf<String, String>()
        var key: String? = null
        var value = StringBuilder()
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    key = parser.name
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == key && key != null) {
                        data[key] = value.toString()
                        value.setLength(0)
                    }
                    else if (parser.name == "data") {
                        done = true
                    }
                }
                XmlPullParser.TEXT -> {
                    value.append(parser.text)
                }
            }
        }
        return data
    }
}