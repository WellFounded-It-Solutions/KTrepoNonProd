package se.infomaker.iap.articleview.transformer

import android.os.Build
import android.util.Xml
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.infomaker.iap.articleview.item.image.ImageGalleryItem
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.transformer.newsml.parser.ImageGalleryParser
import kotlin.test.assertEquals


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ImageGalleryParserTest {

    @Test
    fun testParseImageGallery() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("image_gallery.xml").byteInputStream(), "UTF-8")
        parser.next()

        val list = ImageGalleryParser().parse(parser)
        (list.firstOrNull() as ImageGalleryItem).let {
            assertEquals(it.text, "Titel för mitt gallery")
            assertEquals(it.images.size, 4)

            // Check Image1 properties
            val imageItem1: ImageItem = it.images[0] as ImageItem
            assertEquals("x-im/image",imageItem1.themeKey)
            assertEquals("im://image/H0OGPVaoIlywkCYGP4-f02fxqd4.jpg", imageItem1.uri)
            assertEquals(612, imageItem1.width)
            assertEquals(408, imageItem1.height)
            assertEquals("",  imageItem1.text)
            assertEquals("",  imageItem1.alttext)
            // Check Author properties
            assertEquals(0, imageItem1.authors.size)
            // Check Crop properties
            assertEquals(0, imageItem1.crops.size)

            // Check Image2 properties
            val imageItem2: ImageItem = it.images[1] as ImageItem
            assertEquals("x-im/image",imageItem2.themeKey)
            assertEquals("im://image/HARQoWMVRc4gvphPVeVMEgVLLaM.jpg", imageItem2.uri)
            assertEquals(600, imageItem2.width)
            assertEquals(480, imageItem2.height)
            assertEquals("Kattunge Liten Gullig Sover Rosa",  imageItem2.text)
            assertEquals("This is the alttext",  imageItem2.alttext)
            // Check Author properties
            assertEquals("author", imageItem2.authors[0].themeKey)
            assertEquals(1, imageItem2.authors[0].fields.size)
            assertEquals("Tradera", imageItem2.authors[0].fields[0].content)
            // Check Crop properties
            assertEquals(0, imageItem2.crops.size)

            // Check Image3 properties
            val imageItem3: ImageItem = it.images[2] as ImageItem
            assertEquals("x-im/image",imageItem3.themeKey)
            assertEquals("im://image/y2YA7-dtOO1Wv_hnv7MVp9VfOdg.jpg", imageItem3.uri)
            assertEquals(1280, imageItem3.width)
            assertEquals(925, imageItem3.height)
            assertEquals("Bra tips när du väljer kattunge",  imageItem3.text)
            assertEquals("This is the alttext",  imageItem3.alttext)
            // Check Author properties
            assertEquals("author", imageItem3.authors[0].themeKey)
            assertEquals(1, imageItem3.authors[0].fields.size)
            assertEquals("Försäkra Katten", imageItem3.authors[0].fields[0].content)
            // Check Crop properties
            assertEquals(1, imageItem3.crops.size)
            assertEquals("im://crop/0/0.07879924953095685/0.755/0.6322701688555347", imageItem3.crops.get("16:9"))
        }
    }
}