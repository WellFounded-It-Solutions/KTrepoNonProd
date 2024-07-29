package se.infomaker.iap.articleview.transformer

import android.util.Xml
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.infomaker.iap.articleview.transformer.newsml.parser.LinksParser
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class LinksParserTest {
    @Test
    fun testParse() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("link.xml").byteInputStream(), "UTF-8")
        parser.next()
        val link = LinksParser().parse(parser)
        assertEquals("Erik testar Digital Writer 2", link.first().title)
        assertEquals("x-im/content", link.first().links.first().type)
        assertEquals("0e183278-b388-4cc1-8edc-6ff81698cba8", link.first().links.first().uuid)
    }
}