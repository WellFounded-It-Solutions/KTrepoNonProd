package se.infomaker.iap.statistics

import org.junit.Assert
import org.junit.Test
import se.infomaker.frt.statistics.firebaseanalytics.EventMapping

class EventMappingTests {

    @Test
    fun testOversizedString() {
        val value = EventMapping.safeString("http://www.pressclub.be/wp-content/uploads/2019/04/Letter-of-Summons-EU-Emergency-Trust-Fund-for-Africa-1.pdf")
        Assert.assertEquals("http://www.pressclub.be/wp-content/uploads/2019/04/Letter-of-Summons-EU-Emergency-Trust-Fund-for-Afr", value)
        Assert.assertEquals(value.length, 100)

        val valueWithCommaInRange = EventMapping.safeString("https://secure.n-able.com/webhelp/NC_9-1-0_SO_en/Content/SA_docs/API_Level_Integration/API_Integration_URLEncoding.html")
        Assert.assertEquals("https://secure.n-able.com/webhelp/NC_9-1-0_SO_en/Content/SA_docs/API_Level_Integration/API_Integrati", valueWithCommaInRange)
        Assert.assertEquals(valueWithCommaInRange.length, 100)

        val valueWithCommaOutsideRange = EventMapping.safeString("https://secure.n-able.com/webhelp/NC_9-1-0_SO_en/Content/SA_docs/API_Level_Integration/API_Integration_URL,Encoding.html")
        Assert.assertEquals("https://secure.n-able.com/webhelp/NC_9-1-0_SO_en/Content/SA_docs/API_Level_Integration/API_Integrati", valueWithCommaOutsideRange)
        Assert.assertEquals(valueWithCommaOutsideRange.length, 100)

        val valueWithMultipleComma = EventMapping.safeString("https://secure.n-able.com/webhelp/NC_9-1-0_SO_en/Content/SA,docs/API_Level_Integration/API_Integration_URL,Encoding.html")
        Assert.assertEquals("https://secure.n-able.com/webhelp/NC_9-1-0_SO_en/Content/SA", valueWithMultipleComma)
    }

    @Test
    fun testSimpleResolve() {
        val mapping = EventMapping("name", mapOf())
        val value = mapping.resolve("{{value}}", "N/A", mapOf("value" to "Fuck yeah!"))
        Assert.assertEquals("Fuck yeah!", value)
    }

    @Test
    fun test2levelResolve() {
        val mapping = EventMapping("name", mapOf())
        val value = mapping.resolve("{{nested.value}}", "N/A", mapOf("nested" to mapOf("value" to "Fuck yeah!")))
        Assert.assertEquals("Fuck yeah!", value)
    }

    @Test
    fun missingNestedResolve() {
        val mapping = EventMapping("name", mapOf())
        val value = mapping.resolve("{{nested.novalue}}", "N/A", mapOf("nested" to mapOf("value" to "Fuck yeah!")))
        Assert.assertEquals("N/A", value)
    }

    @Test
    fun combinedValueResolve() {
        val mapping = EventMapping("name", mapOf())
        val value = mapping.resolve("{{title}}: {{nested.first}} - {{nested.second}}", "N/A", mapOf("nested" to mapOf("first" to "1", "second" to "2"), "title" to "result"))
        Assert.assertEquals("result: 1 - 2", value)
    }
}