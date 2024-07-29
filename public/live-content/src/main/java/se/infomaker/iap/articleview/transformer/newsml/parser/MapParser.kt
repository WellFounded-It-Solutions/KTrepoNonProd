package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.decorator.MarginDecorator
import se.infomaker.iap.articleview.item.map.MapItem

class MapParser : ItemParser {

    private fun extractMapData(parser: XmlPullParser): MapItem {

        val regex = ("(\\-?\\d+\\.\\d+)\\s*(\\-?\\d+\\.\\d+)").toRegex()

        var lat = ""
        var lng = ""
        var zoom = ""
        var done = false

        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "zoom" -> {
                            zoom = parser.nextText()
                        }
                        "point" -> {
                            val tempPoint = parser.nextText()
                            val point = regex.find(tempPoint)?.groups?.get(0)?.value?.split(" ")
                            lng = point?.get(0) ?: "0.0"
                            lat = point?.get(1) ?: "0.0"
                        }
                        "geometry" -> {
                            val tempPoint = parser.nextText()
                            val point = regex.find(tempPoint)?.groups?.get(0)?.value?.split(" ")
                            lng = point?.get(0) ?: "0.0"
                            lat = point?.get(1) ?: "0.0"
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "data") {
                        done = true
                    }
                }
            }
        }
        return MapItem("", lat, lng, zoom)
    }

    override fun parse(parser: XmlPullParser): List<Item> {

        val maps = mutableListOf<MapItem>()
        var done = false
        while (!done) {
            val playerAttributes = parser.getAttributes()
            if (playerAttributes["type"] == "x-im/mapembed") {
                val mapItem = extractMapData(parser)
                mapItem.decorators.add(MarginDecorator(
                    top = listOf("mapMarginTop", "mapMarginVertical"),
                    bottom = listOf("mapMarginBottom", "mapMarginVertical")
                ))
                maps.add(mapItem)
            }
            parser.next()
            when (parser.eventType) {
                XmlPullParser.END_TAG -> {
                    if (parser.name == "object") {
                        done = true
                    }
                }
            }
        }
        return maps
    }
}