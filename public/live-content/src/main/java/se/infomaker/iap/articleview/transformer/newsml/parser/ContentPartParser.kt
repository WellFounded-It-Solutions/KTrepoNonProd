package se.infomaker.iap.articleview.transformer.newsml.parser

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.navigaglobal.mobile.livecontent.R
import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.decorator.BackgroundColorDecorator
import se.infomaker.iap.articleview.item.decorator.BackgroundDrawableDecorator
import se.infomaker.iap.articleview.item.decorator.BorderDimensions
import se.infomaker.iap.articleview.item.decorator.DecoratorColors
import se.infomaker.iap.articleview.item.decorator.MarginDecorator
import se.infomaker.iap.articleview.item.decorator.PaddingDecorator
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.element.ElementListItem
import se.infomaker.iap.articleview.item.element.LinkSpannable
import se.infomaker.iap.articleview.item.image.ImageItem
import java.util.Locale
import java.util.UUID

class ContentPartParser(private val elementParser: ElementParser) : ItemParser {

    companion object {
        const val FACT = "fact"
        const val FACT_SUBJECT = "factSubject"
        const val FACT_TITLE = "factTitle"
    }

    private val imageParser = ImageParser()
    private val objectParser = ObjectParser(mapOf("x-im/image" to imageParser))

    @SuppressLint("DefaultLocale")
    override fun parse(parser: XmlPullParser): List<Item> {
        val items = mutableListOf<Item>()
        val attributes = parser.getAttributes()
        val uuid = attributes["id"] ?: UUID.randomUUID().toString()
        var legacyTitle: ElementItem? = null
        attributes["title"]?.let {
            if (it.isNotEmpty()) {
                legacyTitle = ElementItem("${uuid}Title", listOf("title", "element", "default"), mapOf(Pair("backgroundColor", "title")), SpannableStringBuilder(it))
            }
        }
        var links: List<Map<String, String>>? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            when (parser.name) {
                "data" -> {
                    val parseResult = parseData(uuid, parser)
                    if (!parseResult.dataKeys.contains("title")) {
                        legacyTitle?.let {
                            items.add(it)
                        }
                    }
                    items.addAll(parseResult.items)
                }
                "links" -> {
                    links = parseLinks(parser, items)
                }
            }
        }
        links?.let {
            // Decorate
            for (link in it) {
                val uri = link["uri"] ?: ""
                val type = if (uri.contains("/")) { uri.split("/").last() } else null
                if (link["rel"] == "content-part" && type != null) {
                    val backgroundDecoratorColor = DecoratorColors(listOf("${type}Background", "contentPartBackground"))
                    val decorator = BackgroundColorDecorator(backgroundDecoratorColor)
                    val updatedItems = mutableListOf<Item>()
                    items.forEach { item ->
                        item.decorators.add(0, decorator)
                        when (item) {
                            is ElementItem -> {
                                val updatedThemeKeys = item.themeKeys.addOptionalThemeKeys(item.variation?.let { variation -> type + variation.useTheCapsLuke() }, "$type${item.themeKeys.first().useTheCapsLuke()}")
                                val updatedItem = ElementItem(item.uuid, updatedThemeKeys, item.attributes, item.text, type + item.type.useTheCapsLuke(), prefix = type)
                                updatedItem.decorators.addAll(item.decorators)
                                val itemBackgroundDecorator = BackgroundColorDecorator(DecoratorColors(listOf(updatedItem.type + "Background")))
                                updatedItem.decorators.add(itemBackgroundDecorator)
                                updatedItems.add(updatedItem)
                            }
                            is ElementListItem -> {
                                val listThemeKey = item.themeKeys.elementAtOrNull(1)?.apply { type + this.useTheCapsLuke() }
                                val updatedThemeKeys = item.themeKeys.addOptionalThemeKeys(item.variation?.let { variation -> type + variation.useTheCapsLuke() }, listThemeKey)
                                val indicatorListThemeKey = item.indicatorThemeKeys.elementAtOrNull(1)?.apply { type + this.useTheCapsLuke() }
                                val indicatorThemeKeys = item.indicatorThemeKeys.addOptionalThemeKeys(item.variation?.let { variation -> type + variation.useTheCapsLuke() }, indicatorListThemeKey)
                                val updatedItem = ElementListItem(item.uuid, updatedThemeKeys, indicatorThemeKeys, item.attributes, item.elementItems, item.listType, item.variation)
                                val itemBackgroundDecorator = BackgroundColorDecorator(DecoratorColors(listOf("${type}Background")))
                                updatedItem.decorators.add(itemBackgroundDecorator)
                                updatedItems.add(updatedItem)
                            }
                            is ImageItem -> {
                                val updatedItem = item.copy(type = type + "Image", selectorType = type + "Image").apply {
                                    textToShow = listOf("text")
                                }
                                val itemBackgroundDecorator = BackgroundColorDecorator(backgroundDecoratorColor)
                                updatedItem.decorators.add(itemBackgroundDecorator)
                                updatedItems.add(updatedItem)
                            }
                            else -> updatedItems.add(item)
                        }
                    }

                    if (type == FACT) {
                        if (updatedItems.filterIsInstance<ImageItem>().any { imageItem ->  imageItem.contentPartSource }) {
                            val headerElement = updatedItems.first { subject -> !subject.isMatching(mapOf("type" to FACT_SUBJECT)) && !subject.isMatching(mapOf("type" to FACT_TITLE)) }
                            val index = updatedItems.indices.firstOrNull { i -> headerElement == updatedItems[i] } ?: 0
                            updatedItems.move(updatedItems.first { item ->  item is ImageItem }, index)
                        }
                    }

                    val paddingTop = listOf(type + "ContentPartPaddingTop", type + "ContentPartPaddingVertical", "contentPartPaddingTop", "contentPartPaddingVertical")
                    val paddingBottom = listOf(type + "ContentPartPaddingBottom", type + "ContentPartPaddingVertical", "contentPartPaddingBottom", "contentPartPaddingVertical")

                    val marginLeft = listOf(type + "ContentPartMarginLeft", type + "ContentPartMarginHorizontal", "contentPartMarginLeft", "contentPartMarginHorizontal")
                    val marginTop = listOf(type + "ContentPartMarginTop", type + "ContentPartMarginVertical", "contentPartMarginTop", "contentPartMarginVertical")
                    val marginRight = listOf(type + "ContentPartMarginRight", type + "ContentPartMarginHorizontal", "contentPartMarginRight", "contentPartMarginHorizontal")
                    val marginBottom = listOf(type + "ContentPartMarginBottom", type + "ContentPartMarginVertical", "contentPartMarginBottom", "contentPartMarginVertical")

                    updatedItems.first().decorators.add(PaddingDecorator(top = paddingTop))
                    updatedItems.first().decorators.add(MarginDecorator(left = marginLeft, right = marginRight, top = marginTop))
                    updatedItems.first().decorators.add(BackgroundDrawableDecorator(R.drawable.border, backgroundDecoratorColor, DecoratorColors(listOf("${type}BorderTopColor", "contentPartBorderTopColor")), BorderDimensions(top = 5F)))

                    if (updatedItems.size > 2) {
                        updatedItems
                                .drop(1)
                                .dropLast(1)
                                .forEach { updatedItem ->
                                    updatedItem.decorators.add(MarginDecorator(left = marginLeft, right = marginRight))
                                }
                    }

                    updatedItems.last().decorators.add(MarginDecorator(left = marginLeft, right = marginRight, bottom = marginBottom))
                    updatedItems.last().decorators.add(PaddingDecorator(bottom = paddingBottom))
                    updatedItems.last().decorators.add(BackgroundDrawableDecorator(R.drawable.border, backgroundDecoratorColor, DecoratorColors(listOf("${type}BorderBottomColor", "contentPartBorderBottomColor")), BorderDimensions(bottom = 2f)))
                    return updatedItems
                }
            }
        }
        return items
    }

    private fun parseLinks(parser: XmlPullParser, items: MutableList<Item>): List<Map<String, String>> {
        val links = mutableListOf<Map<String, String>>()
        while (!(parser.next() == XmlPullParser.END_TAG && "links" == parser.name)) {
            when (parser.name) {
                "link" -> {
                    parser.getAttributes().apply {
                        if (this["rel"] == "image" && this["type"] == "x-im/image") {
                            imageParser.extractImage(parser, parser.getAttributes())?.apply {
                                contentPartSource = true
                            }?.let { imageItem ->
                                items.add(imageItem)
                            }
                        }
                    }
                    links.add(parser.getAttributes())
                    skip(parser)
                }
            }
        }
        return links
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType == XmlPullParser.START_TAG) {
            var depth = 1
            while (depth != 0) {
                when (parser.next()) {
                    XmlPullParser.END_TAG -> depth--
                    XmlPullParser.START_TAG -> depth++
                }
            }
        }
    }

    private data class DataParseResult(val dataKeys: List<String>, val items: List<Item>)

    private fun parseData(uuid: String, parser: XmlPullParser): DataParseResult {
        val items = mutableListOf<Item>()
        val keys = mutableListOf<String>()
        while (parser.next() != XmlPullParser.END_TAG) {
            when (parser.name) {
                "text" -> {
                    keys.add("text")
                    var done = false
                    while (!done) {
                        parser.next()
                        when (parser.eventType) {
                            XmlPullParser.START_TAG -> {
                                when (parser.name) {
                                    IdfParser.ELEMENT_TAG -> items.addAll(elementParser.parse(parser))
                                    IdfParser.OBJECT_TAG -> items.addAll(objectParser.parse(parser))
                                }
                            }
                            XmlPullParser.END_TAG -> {
                                when (parser.name) {
                                    "text" -> done = true
                                }
                            }
                        }
                    }
                }
                else -> {
                    parser.name?.let {
                        keys.add(it)
                        items.add(parseTag(uuid, it, parser))
                    }

                }
            }
        }
        return DataParseResult(keys, items)
    }

    private fun parseTag(uuid: String, tag: String, parser: XmlPullParser): Item {
        val stringBuilder = SpannableStringBuilder()
        var done = false
        var emStart = 0
        var strongStart = 0
        var linkStart = 0
        var linkAttributes: Map<String, String>? = null
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "em" -> emStart = stringBuilder.length
                        "strong" -> strongStart = stringBuilder.length
                        "a" -> {
                            linkAttributes = parser.getAttributes()
                            linkStart = stringBuilder.length
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        tag -> done = true
                        "em" -> stringBuilder.setSpan(StyleSpan(Typeface.ITALIC), emStart, stringBuilder.length, 0)
                        "strong" -> stringBuilder.setSpan(StyleSpan(Typeface.BOLD), strongStart, stringBuilder.length, 0)
                        "a" -> {
                            stringBuilder.setSpan(LinkSpannable(linkAttributes), linkStart, stringBuilder.length, 0)
                        }
                    }
                }
                XmlPullParser.TEXT -> stringBuilder.append(parser.text)
            }
        }
        return ElementItem("${uuid}${tag.useTheCapsLuke()}", listOf(tag, "element", "default"), mapOf(Pair("backgroundColor", tag)), stringBuilder, tag)
    }
}

private fun String.useTheCapsLuke(): String =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

/**
 * Moves the given **T** item to the specified index
 */
fun <T> MutableList<T>.move(item: T, newIndex: Int) {
    val currentIndex = indexOf(item)
    if (currentIndex < 0) return
    removeAt(currentIndex)
    add(newIndex, item)
}
