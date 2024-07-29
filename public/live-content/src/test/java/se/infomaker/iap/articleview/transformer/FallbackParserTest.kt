package se.infomaker.iap.articleview.transformer

import android.util.Xml
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.infomaker.iap.articleview.item.fallback.FallbackItem
import se.infomaker.iap.articleview.item.fallback.FallbackItemParser
import se.infomaker.iap.articleview.item.unsupported.UnsupportedItem
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class FallbackParserTest {
    @Test
    fun testParseInstagram() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("fallback1.xml").byteInputStream(), "UTF-8")
        parser.next()
        val list = FallbackItemParser().parseOrFallback(parser, { type, attributes ->
            listOf(UnsupportedItem(type, attributes))
        })
        (list.firstOrNull() as FallbackItem).let {
            assertEquals("Klicka för att visa innehåll", it.title)
            assertEquals("https://scontent-lht6-1.cdninstagram.com/t51.2885-15/s640x640/sh0.08/e35/21107437_2085995158093216_4062982389922529280_n.jpg", it.imageUrl)
            assertEquals("https://www.instagram.com/p/BYVP8xPjYUS/?hl=en", it.webUrl)
            assertEquals(200, it.imageHeight)
            assertEquals(400, it.imageWidth)
        }
    }

    @Test
    fun testParseTwitter() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("fallback2.xml").byteInputStream(), "UTF-8")
        parser.next()
        val list = FallbackItemParser().parseOrFallback(parser, { type, attributes ->
            listOf(UnsupportedItem(type, attributes))
        })
        (list.firstOrNull() as FallbackItem).let {
            assertEquals("Klicka för att visa innehåll", it.title)
            assertEquals(null, it.imageUrl)
            assertEquals("https://twitter.com/JakeWharton/status/895853680422821888", it.webUrl)
            assertNull(it.imageHeight)
            assertNull(it.imageWidth)
            assertNull(it.imageUrl)
        }
    }
}