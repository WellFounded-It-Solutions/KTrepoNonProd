package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.image.ImageItem
import java.util.UUID

class ImageParser : ItemParser {

    private val authorParser = AuthorParser()
    private val elementParser = ElementParser()

    override fun parse(parser: XmlPullParser): List<Item> {

        val images  = mutableListOf<Item>()
        var done = false
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "link" -> {
                            val attributes = parser.getAttributes()
                            when (attributes["type"]) {
                                "x-im/image" -> {
                                    extractImage(parser, attributes)?.let { imageItem ->
                                        images.add(imageItem)
                                    }
                                }
                            }
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
        return images
    }

    fun extractImage(parser: XmlPullParser, imageAttributes: Map<String, String>):  ImageItem? {
        val builder = ImageItem.builder {
            type = imageAttributes["type"] ?: type
            uuid = imageAttributes["uuid"]
            uri  = imageAttributes["uri"]  ?: uri
        }

        var linksCount = 0
        var done = false
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "links" -> {
                            linksCount++
                        }
                        "link" -> {
                            val attributes = parser.getAttributes()
                            when (attributes["type"]) {
                                "x-im/author" -> {
                                    builder.authors.add(authorParser.parse(parser).first())
                                }
                                "x-im/crop" -> {
                                    val uri = attributes["uri"]
                                    val title = attributes["title"]
                                    if (uri != null && title != null) {
                                        builder.crops[title] = uri
                                    }
                                }
                            }
                        }
                        "text" -> {
                            val textElement = elementParser.extractSingleElement(parser, parser.name)
                            builder.text = textElement.text.toString()
                            builder.textElement = textElement
                        }
                        "alttext" -> {
                            builder.alttext = parser.nextText()
                        }
                        "width" -> {
                            builder.width = parser.nextText().toInt()
                        }
                        "height" -> {
                            builder.height = parser.nextText().toInt()
                        }
                        "flags" -> {
                            builder.disableAutomaticCrop = extractAutoCropFlag(parser)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "link" && linksCount == 0) {
                        done = true
                    }
                    if (parser.name == "links") {
                        linksCount--
                    }
                }
            }
        }
        return builder.build()
    }

    private fun extractAutoCropFlag(parser: XmlPullParser): Boolean {
        var done = false
        var disableAutoCropping = false
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "flag") {
                        when (parser.nextText()) {
                            "disableAutomaticCrop" -> {
                                disableAutoCropping = true
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    done = true
                }
            }
        }
        return disableAutoCropping
    }
}