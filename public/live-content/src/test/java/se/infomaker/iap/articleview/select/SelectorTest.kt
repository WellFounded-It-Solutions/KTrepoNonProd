package se.infomaker.iap.articleview.select

import android.text.SpannableStringBuilder
import org.json.JSONObject
import org.junit.Test
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.ContentViewModel
import se.infomaker.iap.articleview.item.author.AuthorItem
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.item.links.LinksItem
import se.infomaker.iap.articleview.preprocessor.select.Selector
import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig
import kotlin.test.assertEquals

class SelectorTest {
    companion object {
        val in_ = ContentStructure(
                body = ContentViewModel(
                        items = mutableListOf(
                                ElementItem("myFirstText", listOf(), mapOf("type" to "headline"), SpannableStringBuilder("This is my first text")),
                                ImageItem("myImage", "x-im/image", "", 0, 0, "", "", mapOf(), mutableListOf()),
                                ElementItem("mySecondText", listOf(), mapOf("type" to "body"), SpannableStringBuilder("This is my second text")),
                                ElementItem("mySecondSecondText", listOf(), mapOf("type" to "body"), SpannableStringBuilder("This is my second second text")),
                                ElementItem("myThirdText", listOf(), mapOf(), SpannableStringBuilder("This is my third text")),
                                ImageItem("myImage2", "x-im/image", "", 0, 0, "", "", mapOf(), mutableListOf()),
                                LinksItem(mapOf("type" to "list"), listOf()),
                                ElementItem("myFourthText", listOf(), mapOf("type" to "smelly cat"), SpannableStringBuilder("This is my fourth text")),
                                AuthorItem("myId"))),
                properties = JSONObject()).body.items
    }

    @Test
    fun testSelectType() {
        assertEquals(listOf(1, 5), Selector.getIndexes(in_, SelectorConfig(type = "image")))
        assertEquals(listOf(0, 2, 3, 4, 7), Selector.getIndexes(in_, SelectorConfig(type = "element")))
        assertEquals(listOf(8), Selector.getIndexes(in_, SelectorConfig(type = "author")))
    }

    @Test
    fun testSelectSubset() {
        assertEquals(listOf(3), Selector.getIndexes(in_, SelectorConfig(subset = 3)))
        assertEquals(listOf(4), Selector.getIndexes(in_, SelectorConfig(subset = 4)))
        assertEquals(listOf(6), Selector.getIndexes(in_, SelectorConfig(subset = "6")))
        assertEquals(listOf(7), Selector.getIndexes(in_, SelectorConfig(subset = 7)))
        assertEquals(listOf(), Selector.getIndexes(in_, SelectorConfig(subset = 9)))
        assertEquals(listOf(), Selector.getIndexes(in_, SelectorConfig(subset = -5)))
        assertEquals(listOf(0), Selector.getIndexes(in_, SelectorConfig(subset = "first")))
        assertEquals(listOf(8), Selector.getIndexes(in_, SelectorConfig(subset = "last")))

        assertEquals(listOf(0, 8), Selector.getIndexes(in_, SelectorConfig(subset = "first|last")))
        assertEquals(listOf(3, 8), Selector.getIndexes(in_, SelectorConfig(subset = "last|3")))
        assertEquals(listOf(4, 6, 8), Selector.getIndexes(in_, SelectorConfig(subset = "4|6|8")))
        assertEquals(listOf(4, 6, 8), Selector.getIndexes(in_, SelectorConfig(subset = "4|6|8|last")))

        //Running on empty list
        assertEquals(listOf(), Selector.getIndexes(listOf(), SelectorConfig(subset = "4|6|7|last")))
    }

    @Test
    fun testSelectMatching() {
        assertEquals(listOf(0), Selector.getIndexes(in_, SelectorConfig(matching = mapOf("type" to "headline"))))
        assertEquals(listOf(2, 3), Selector.getIndexes(in_, SelectorConfig(matching = mapOf("type" to "body"))))
        assertEquals(listOf(7), Selector.getIndexes(in_, SelectorConfig(matching = mapOf("type" to "smelly cat"))))

        assertEquals(listOf(), Selector.getIndexes(in_, SelectorConfig(matching = mapOf("type" to "fail"))))
        assertEquals(listOf(), Selector.getIndexes(in_, SelectorConfig(matching = mapOf("fail" to "fail"))))
        assertEquals(listOf(), Selector.getIndexes(in_, SelectorConfig(matching = mapOf("type" to "headline", "fail" to "fail"))))
    }

    @Test
    fun testSelectTypeSubset() {
        assertEquals(listOf(0), Selector.getIndexes(in_, SelectorConfig(
                type = "element",
                subset = "first")))
        assertEquals(listOf(7), Selector.getIndexes(in_, SelectorConfig(
                type = "element",
                subset = "last")))
        assertEquals(listOf(0, 7), Selector.getIndexes(in_, SelectorConfig(
                type = "element",
                subset = "last|first")))
        assertEquals(listOf(0, 2, 7), Selector.getIndexes(in_, SelectorConfig(
                type = "element",
                subset = "last|first|5|1")))
        assertEquals(listOf(5), Selector.getIndexes(in_, SelectorConfig(
                type = "image",
                subset = "last")))
    }

    @Test
    fun testSelectTypeMatching() {
        assertEquals(listOf(0), Selector.getIndexes(in_, SelectorConfig(
                type = "element",
                matching = mapOf("type" to "headline"))))
        assertEquals(listOf(2, 3), Selector.getIndexes(in_, SelectorConfig(
                type = "element",
                matching = mapOf("type" to "body"))))
        assertEquals(listOf(), Selector.getIndexes(in_, SelectorConfig(
                type = "image",
                matching = mapOf("type" to "myImage"))))
    }

    @Test
    fun testSelectMatchingSubset() {
        assertEquals(listOf(3), Selector.getIndexes(in_, SelectorConfig(
                subset = "1",
                matching = mapOf("type" to "body"))))
        assertEquals(listOf(2), Selector.getIndexes(in_, SelectorConfig(
                subset = "0",
                matching = mapOf("type" to "body"))))
        assertEquals(listOf(2), Selector.getIndexes(in_, SelectorConfig(
                subset = "first",
                matching = mapOf("type" to "body"))))
        assertEquals(listOf(2, 3), Selector.getIndexes(in_, SelectorConfig(
                subset = "first|last",
                matching = mapOf("type" to "body"))))
        assertEquals(listOf(2), Selector.getIndexes(in_, SelectorConfig(
                subset = "first|0",
                matching = mapOf("type" to "body"))))
    }

    @Test
    fun testSelectTypeMatchingSubset() {
        assertEquals(listOf(3), Selector.getIndexes(in_, SelectorConfig(
                type = "element",
                subset = "1",
                matching = mapOf("type" to "body"))))
        assertEquals(listOf(), Selector.getIndexes(in_, SelectorConfig(
                type = "image",
                subset = "1",
                matching = mapOf("type" to "body"))))
    }
}