package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item
import timber.log.Timber

/**
 * Parses and consumes one parser
 */
interface ItemParser {
    fun parse(parser: XmlPullParser) : List<Item>

}

fun ItemParser.safeParse(tag: String, parser: XmlPullParser) : List<Item> {
    try {
        return parse(parser)
    }
    catch (e: Throwable) {
        Timber.e(e, "Failed to parse tag:$tag")
        // Skip until the object is passed
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == tag)) {
            parser.next()
        }
    }
    return mutableListOf()
}