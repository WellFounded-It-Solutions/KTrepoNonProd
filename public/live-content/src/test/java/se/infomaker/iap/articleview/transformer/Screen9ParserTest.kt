package se.infomaker.iap.articleview.transformer

import android.os.Build
import android.util.Xml
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.infomaker.iap.articleview.item.screen9.Screen9Item
import se.infomaker.iap.articleview.transformer.newsml.parser.Screen9Parser
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class Screen9ParserTest {
    @Test
    fun testParsePlayerObject() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("screen9.xml").byteInputStream(), "UTF-8")
        parser.next()

        val list = Screen9Parser().parse(parser)
        (list.firstOrNull() as Screen9Item).let {

            assertEquals(544, it.height)
            assertEquals(856, it.width)
            assertEquals("https://csp.screen9.com/iframe/ZziAx_LRxU57Vv3wIZ6k-M0PX4m4SJbk5OXkT-22uKPNuI4x6WSN4kpJmg9AL0oX", it.videoUrl)
            assertEquals("mNgxMszFIyPp3_DA26yQdw", it.mediaid)
            assertEquals("703937", it.accountid)
            assertEquals("KSFtest.mov", it.title)
            assertEquals("", it.description)
            assertEquals("screen9", it.providerName)
            assertEquals(3, it.duration)
            assertEquals("https://csp.screen9.com/img/m/N/g/x/thumb_mNgxMszFIyPp3_DA26yQdw/2.jpg?v=0", it.thumbnailUrl)
            assertEquals("KSFtest.mov", it.title)
            assertEquals("<div style=\"position:relative;width:100%;padding-bottom:63.551%;\"><iframe allowfullscreen frameborder=\"0\" src=\"//csp.screen9.com/iframe/ZziAx_LRxU57Vv3wIZ6k-M0PX4m4SJbk5OXkT-22uKPNuI4x6WSN4kpJmg9AL0oX\" style=\"width:100%;height:100%;position:absolute\"></iframe></div>", it.html)
        }
    }
}