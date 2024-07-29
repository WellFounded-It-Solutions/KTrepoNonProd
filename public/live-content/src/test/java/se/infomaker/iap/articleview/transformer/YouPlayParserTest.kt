package se.infomaker.iap.articleview.transformer

import android.util.Xml
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.infomaker.iap.articleview.item.youplay.YouPlayItem
import se.infomaker.iap.articleview.transformer.newsml.parser.YouPlayParser
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class YouPlayParserTest {

    @Test
    fun testParsePlayerObject() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("youplay.xml").byteInputStream(), "UTF-8")
        parser.next()

        val list = YouPlayParser().parse(parser)
        (list.firstOrNull() as YouPlayItem).let {
            assertEquals("x-im/youplay", it.type)
//            assertEquals("video", it.mediaType)
//            assertEquals("undefined", it.playerId)
//            assertEquals("Flowplayer", it.provider)
//            assertEquals("Flowplayer", it.provider)
//            assertEquals("2018-08-23T10:30:42+0000", it.publishDate)
//            assertEquals("https://cf97d8675.lwcdn.com/i/v-i-257ff9ce-d51b-4f27-984f-7b319d19e9a2-4.jpg", it.thumbnailUrl)
//            assertEquals("https://ljsp.lwcdn.com/api/video/embed.jsp?id=257ff9ce-d51b-4f27-984f-7b319d19e9a2", it.url)
//            assertEquals("Oscar Möller skriver nytt fyraårskontrakt", it.title)
//            assertEquals("<div style=\"left: 0; width: 100%; height: 0; position: relative; padding-bottom: 56.2493%;\">\n" +
//                    "                            <iframe style=\"border: 0; top: 0; left: 0; width: 100%; height: 100%; position: absolute;\" src=\"https://ljsp.lwcdn.com/api/video/embed.jsp?id=257ff9ce-d51b-4f27-984f-7b319d19e9a2\"\n" +
//                    "                            title=\"0;\" byline=\"0;\" portrait=\"0;\" frameborder=\"0\" allowfullscreen=\"\" scrolling=\"no\"></iframe></div>", it.embedCode)
        }
    }
}