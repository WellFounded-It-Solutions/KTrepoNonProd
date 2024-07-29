package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.links.Link
import se.infomaker.iap.articleview.item.links.LinksItem

class LinksParser : ItemParser {
    override fun parse(parser: XmlPullParser): List<LinksItem> {
        val attributes = parser.getAttributes()
        val links = mutableListOf<Link>()
        var done = false
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if  (parser.name == "link") {
                        val attributes = parser.getAttributes()
                        links.add(Link(attributes))
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "object") {
                        done = true
                    }
                }
            }
        }
        return listOf(LinksItem(attributes, links))
    }
}