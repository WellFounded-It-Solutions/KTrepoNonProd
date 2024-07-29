package se.infomaker.iap.articleview.transformer

import android.os.Build
import android.util.Xml
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.transformer.newsml.parser.ImageParser
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ImageParserTest {

    @Test
    fun testParseImage() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("image.xml").byteInputStream(), "UTF-8")
        parser.next()

        val list = ImageParser().parse(parser)
        (list.firstOrNull() as ImageItem).let {

            // Check Image properties
            assertEquals("x-im/image", it.themeKey)
            assertEquals("a65611d9-5304-5a5a-862e-e5cd15802c9f", it.id)
            assertEquals("im://image/55yYAeWS2VX02B7FDsftfcNKCcM.jpg", it.uri)
            assertEquals(4480, it.width)
            assertEquals(3001, it.height)
            assertEquals("This is the main text with formatting.", it.text)
            assertEquals("This is alternative text.", it.alttext)

            // Check Author properties
            assertEquals("Kirsty Wigglesworth", it.authors[0].fields[0].content)
            assertEquals("Boaty McBoatFace", it.authors[1].fields[0].content)

            // Check Crop properties
            assertEquals(3, it.crops.size)
            assertEquals(it.crops.get("2:1"), "im://crop/0/0.15858208955223882/0.67/0.4962686567164179")
            assertEquals(it.crops.get("3:2"), "im://crop/0/0.0037313432835820895/1/0.9944029850746269")
            assertEquals(it.crops.get("2:3"), "im://crop/0.0675/0/0.44625/1")
        }
    }
}