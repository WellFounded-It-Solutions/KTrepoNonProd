package se.infomaker.iap.action.display.flow

import junit.framework.Assert
import org.junit.Test

class TestJSONEscaper {

    @Test
    fun testJSONEscaper() {
        val lineBroken = "this \n is \n a \nlinebroken \n string"
        val expected = "this \\n is \\n a \\nlinebroken \\n string"
        val escape = JSONEscaper.escape(lineBroken)
        Assert.assertEquals(expected, escape)
    }
}