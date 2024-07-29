package se.infomaker.iap.articleview.select

import com.google.gson.Gson
import org.junit.Test
import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig
import kotlin.test.assertEquals

class ParseSelectorTest {
    companion object {
        val JSON_OBJECT = "{" +
                "\"type\" : \"element\"," +
                "\"subset\": \"first|3\"," +
                "\"matching\": {\"type\": \"headline\"}" +
                "}"
    }

    @Test
    fun testParseSelector() {
        val config = Gson().fromJson(JSON_OBJECT, SelectorConfig::class.java)
        assertEquals("headline", config.matching["type"])
        assertEquals(listOf(0, 3), config.getSubset())
        assertEquals("element", config.type)
    }
}