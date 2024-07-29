package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.END_DOCUMENT

fun XmlPullParser.getAttributes(): Map<String, String> {
    val attributes = mutableMapOf<String, String>()
    for (i in 0 until attributeCount) {
        attributes.put(getAttributeName(i), getAttributeValue(i))
    }
    return attributes
}

fun XmlPullParser.forwardTo(tag: Int, name: String) {
    while (true) {
        next()
        if (eventType == END_DOCUMENT || (eventType == tag && getName() == name)) {
            return
        }
    }
}

