package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item

class IdfParser(private val elementParser: ElementParser = ElementParser(), private val objectParser: ObjectParser, private val groupParser: GroupParser) {
    companion object {
        const val ELEMENT_TAG = "element"
        const val OBJECT_TAG = "object"
        const val GROUP_TAG = "group"
    }

    fun parse(parser: XmlPullParser): MutableList<Item> {
        val items = mutableListOf<Item>()
        while (parser.next() != XmlPullParser.END_TAG) {
            when (parser.name) {
                ELEMENT_TAG -> items.addAll(elementParser.safeParse(ELEMENT_TAG, parser))
                OBJECT_TAG -> items.addAll(objectParser.safeParse(OBJECT_TAG, parser))
                GROUP_TAG -> items.addAll(groupParser.safeParse(GROUP_TAG, parser))
            }
        }
        return items
    }
}