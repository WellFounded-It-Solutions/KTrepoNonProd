package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.decorator.GroupDecorator
import se.infomaker.iap.articleview.item.Item

/**
 * Parse group of items and decorate them with the group attributes
 */
class GroupParser(private val elementParser: ElementParser = ElementParser(), private val objectParser: ObjectParser) : ItemParser {
    override fun parse(parser: XmlPullParser): List<Item> {
        val groupDecorator = GroupDecorator(parser.getAttributes())
        val items = mutableListOf<Item>()
        while (parser.next() != XmlPullParser.END_TAG) {
            when(parser.name) {
                IdfParser.ELEMENT_TAG -> {
                    val elements = elementParser.safeParse(parser.name, parser)
                    elements.forEach { it.decorators.add(0, groupDecorator) }
                    items.addAll(elements)

                }
                IdfParser.OBJECT_TAG -> {
                    val objects = objectParser.safeParse(parser.name, parser)
                    objects.forEach { it.decorators.add(0, groupDecorator) }
                    items.addAll(objects)
                }
                IdfParser.GROUP_TAG -> {
                    val groupItems = safeParse(parser.name, parser)
                    groupItems.forEach { it.decorators.add(0, groupDecorator) }
                    items.addAll(groupItems)
                }
            }
        }
        return items
    }
}