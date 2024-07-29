package se.infomaker.iap.articleview.follow.author

import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.follow.FollowPropertyObjectItem
import se.infomaker.iap.articleview.follow.extensions.wrapInJSON
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.transformer.newsml.parser.getAttributes
import se.infomaker.livecontentmanager.parser.PropertyObject
import timber.log.Timber

data class AuthorData(val uuid: String?, val title: String?, val properties: JSONObject) {

    private val propertyObject by lazy { PropertyObject(properties, uuid ?: PropertyObject.NO_UUID) }

    fun toItem(config: AuthorPreprocessorConfig): Item {
        return FollowPropertyObjectItem(propertyObject, config.template, config.selectorType, config.articleProperty, "", "")
    }

    companion object {

        fun fromXml(xml: String?, parser: XmlPullParser): AuthorData? {

            if (xml == null) {
                return null
            }

            parser.setInput(xml.byteInputStream(), "UTF-8")

            var uuid: String? = null
            var title: String? = null
            val authorProperties = JSONObject()
            var done = false
            var currentTag = ""
            while (!done) {
                parser.next()
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "link" -> {
                                val attributes = parser.getAttributes()
                                uuid = attributes["uuid"]
                                title = attributes["title"]
                                authorProperties.putOpt("name", title.wrapInJSON()) // TODO Make sure this is an OK assumption.
                            }
                            else -> currentTag = parser.name
                        }
                    }
                    XmlPullParser.TEXT -> {
                        authorProperties.putOpt(currentTag, parser.text.wrapInJSON())
                    }
                    XmlPullParser.END_DOCUMENT -> done = true
                }
            }
            return AuthorData(uuid, title, authorProperties).also {
                Timber.d("Done, parsed author: $it")
            }
        }
    }
}