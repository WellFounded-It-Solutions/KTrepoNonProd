package se.infomaker.iap.articleview.transformer



import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.transformer.newsml.NewsMLTransformerManager
import se.infomaker.iap.articleview.transformer.newsml.parser.ItemParser
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class NewsMLTransformerManagerTest {

    class TestItem(override val matchingQuery: Map<String, String>, override val selectorType: String) : Item(UUID.randomUUID().toString()) {
        override val typeIdentifier = TestItem::class.java
    }

    @Test
    fun useCustomObjectParser() {
        NewsMLTransformerManager.registerObjectParser("test", object : ItemParser {
            override fun parse(parser: XmlPullParser) : List<Item> {
                return listOf(TestItem(emptyMap(), ""))
            }
        })
        val transformer = NewsMLTransformerManager.createTransformer("newsML")
        val properties = ResourceHelper.createProperties("newsML", "custom_object.xml")

        val articleStructure = transformer.transform(properties.toJSONObject())
        val body = articleStructure.body
        assertEquals(1, body.items.size)
        assertTrue(body.items[0] is TestItem)
    }

    companion object {
        fun Map<String, List<String>>.toJSONObject() : JSONObject {
            val jsonObject = JSONObject()
            for (key in keys) {

                get(key)?.forEach {
                    val list = get(key)
                    val jsonArray = JSONArray()
                    list?.forEach {
                        jsonArray.put(it)
                    }
                    jsonObject.put(key, jsonArray)
                }
            }
            return jsonObject
        }
    }
}