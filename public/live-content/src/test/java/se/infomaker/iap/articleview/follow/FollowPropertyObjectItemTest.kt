package se.infomaker.iap.articleview.follow

import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import se.infomaker.iap.articleview.item.unsupported.UnsupportedItem
import se.infomaker.livecontentmanager.parser.PropertyObject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class FollowPropertyObjectItemTest {

    private lateinit var item: FollowPropertyObjectItem
    private lateinit var anotherItem: FollowPropertyObjectItem

    @Before
    fun setUp() {
        item = FollowPropertyObjectItem(PropertyObject(JSONObject("{}"), "null"), "none", "selector", "articleProp", "val", "title", null)
        anotherItem = FollowPropertyObjectItem(PropertyObject(JSONObject("{}"), "null"), "another template", "selector", "articleProp", "val", "another title", null)
    }

    @Test
    fun `test equality same object`() {
        assertEquals(item, item)
    }

    @Test
    fun `test property mismatch equality failure`() {
        assertNotEquals(item, anotherItem)
    }

    @Test
    fun `test different type equality failure`() {
        assertFalse { item.equals(UnsupportedItem(null, emptyMap())) }
    }
}