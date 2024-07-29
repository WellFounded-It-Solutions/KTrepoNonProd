package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.author.AuthorItem
import timber.log.Timber
import java.util.*

class AuthorParser : ItemParser {
    override fun parse(parser: XmlPullParser): List<AuthorItem> {
        val attributes = parser.getAttributes()
        val title = attributes["title"]
        val uuid = attributes["id"] ?: attributes["uuid"]
        if (title != null) {
            return listOf(AuthorItem(id = uuid ?: UUID.randomUUID().toString(), fields = listOf(AuthorItem.Field(title))))
        }
        Timber.w("Failed to parse author $parser")
        return listOf(AuthorItem.NO_AUTHOR)
    }
}