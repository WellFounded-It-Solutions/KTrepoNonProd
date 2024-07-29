package se.infomaker.iap.articleview.transformer.newsml.parser

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.element.LinkSpannable
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.span.ThemeableBackgroundColorSpan
import se.infomaker.iap.theme.span.ThemeableStyleSpan

interface SpanExtractor {

    val tagName: String

    fun start(parser: XmlPullParser, position: Int)
    fun end(stringBuilder: SpannableStringBuilder, position: Int): SpannableStringBuilder
}

class InsExtractor: SpanExtractor {

    private var insStart = 0
    override val tagName = "ins"

    override fun start(parser: XmlPullParser, position: Int) {
        insStart = position
    }

    override fun end(stringBuilder: SpannableStringBuilder, position: Int): SpannableStringBuilder {
        stringBuilder.delete(insStart, position)
        return stringBuilder
    }
}

class AExtractor : SpanExtractor {

    var linkStart = 0
    var linkAttributes : Map<String, String>? = null

    override val tagName = "a"


    override fun start(parser: XmlPullParser, position: Int) {
        linkAttributes = parser.getAttributes()
        linkStart = position
    }

    override fun end(stringBuilder: SpannableStringBuilder, position: Int): SpannableStringBuilder {
        stringBuilder.setSpan(LinkSpannable(linkAttributes), linkStart, stringBuilder.length, 0)
        return stringBuilder
    }
}

class EmExtractor : SpanExtractor {

    var emStart = 0

    override val tagName = "em"

    override fun start(parser: XmlPullParser, position: Int) {
        emStart = position
    }

    override fun end(stringBuilder: SpannableStringBuilder, position: Int): SpannableStringBuilder {
        stringBuilder.setSpan(ThemeableStyleSpan(listOf("em"), StyleSpan(Typeface.ITALIC)), emStart, stringBuilder.length, 0)
        return stringBuilder
    }
}

class StrongExtractor : SpanExtractor {

    var strongStart = 0

    override val tagName = "strong"

    override fun start(parser: XmlPullParser, position: Int) {
        strongStart = position
    }

    override fun end(stringBuilder: SpannableStringBuilder, position: Int): SpannableStringBuilder {
        stringBuilder.setSpan(ThemeableStyleSpan(listOf("strong"), StyleSpan(Typeface.BOLD)), strongStart, stringBuilder.length, 0)
        return stringBuilder
    }
}

class MarkExtractor : SpanExtractor {
    override val tagName = "mark"

    var markStart = 0

    override fun start(parser: XmlPullParser, position: Int) {
        markStart = position
    }

    override fun end(stringBuilder: SpannableStringBuilder, position: Int): SpannableStringBuilder {
        val backgroundColorSpan = ThemeableBackgroundColorSpan(listOf("markBackground"), ThemeColor.YELLOW)
        stringBuilder.setSpan(backgroundColorSpan, markStart, stringBuilder.length, 0)
        return stringBuilder
    }
}