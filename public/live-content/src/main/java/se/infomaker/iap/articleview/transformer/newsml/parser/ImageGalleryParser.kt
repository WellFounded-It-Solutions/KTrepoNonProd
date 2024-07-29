package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.image.ImageGalleryItem
import java.util.UUID

class ImageGalleryParser : ItemParser {

    private val imageParser = ImageParser()
    private val elementParser = ElementParser()

    override fun parse(parser: XmlPullParser): List<ImageGalleryItem> {

        val builder = ImageGalleryItem.builder {
            type = parser.getAttributes()["type"] ?: type
            uuid = parser.getAttributes()["id"] ?: "Detta är trasigt ${UUID.randomUUID()}"
        }

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
                                    builder.images.add(imageParser.extractImage(parser, attributes))
                                }
                            }
                        }
                        "text" -> {
                            elementParser.extractSingleElement(
                                parser, "text", "text-element-${builder.uuid}"
                            ).also {
                                builder.text = it.text.toString()
                                builder.textElement = it
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
        return builder.build()?.let { listOf(it) }
            ?: emptyList()
    }
}