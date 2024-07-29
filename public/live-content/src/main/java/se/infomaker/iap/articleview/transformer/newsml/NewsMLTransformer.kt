package se.infomaker.iap.articleview.transformer.newsml

import android.util.Xml
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.ContentViewModel
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.transformer.Transformer
import se.infomaker.iap.articleview.transformer.newsml.parser.IdfParser


/**
 * Transforms newsml from a property
 */
class NewsMLTransformer(private val idfParser: IdfParser, private val newsMLPropertyKey: String) : Transformer {
    companion object {
        const val NEWS_ITEM_TAG = "newsItem"
        const val IDF_TAG = "idf"
    }

    override fun transform(properties: JSONObject): ContentStructure {
        properties.optJSONArray(newsMLPropertyKey)?.let {
            if (it.length() > 0) {
                return ContentStructure(parse(it.getString(0)), properties)
            }
        }

        return ContentStructure(properties = properties)
    }

    private fun parse(newsML: String): ContentViewModel {
        val parser = Xml.newPullParser()
        parser.setInput(newsML.byteInputStream(), "UTF-8")
        var done = false
        var items: MutableList<Item>? = null
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == IDF_TAG) {
                        items = idfParser.parse(parser)
                        done = true
                    }
                }
                XmlPullParser.END_DOCUMENT -> {
                    done = true
                }
            }
        }
        return if (items == null ) ContentViewModel() else ContentViewModel(items)
    }
}