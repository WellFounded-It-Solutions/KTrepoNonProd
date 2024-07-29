package se.infomaker.iap.articleview.transformer

import android.util.Xml
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.infomaker.iap.articleview.item.map.MapItem
import se.infomaker.iap.articleview.transformer.newsml.parser.MapParser
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class MapParserTest {

    @Test
    fun testParseMapObject() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("map.xml").byteInputStream(), "UTF-8")
//        parser.next()

        val list = MapParser().parse(parser)
        (list.firstOrNull() as MapItem).let {
            assertEquals("x-im/mapembed", it.id)
        }
    }
}