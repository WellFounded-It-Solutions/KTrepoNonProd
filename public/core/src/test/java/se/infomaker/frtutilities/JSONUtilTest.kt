package se.infomaker.frtutilities

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class JSONUtilTest {

    @Test
    fun testPutStringAtKeyPath() {
        val json = JSONObject()
        JSONUtil.put(json, "my.key.path", "test")

        val string = JSONUtil.getString(json, "my.key.path")
        Assert.assertEquals("test", string)
    }

    @Test
    fun testPutAnyAtKeyPath() {
        val json = JSONObject()
        JSONUtil.put(json, "my.key.path", JSONObject().also { it.put("test", "passed") })

        val jsonObject = JSONUtil.getJSONObject(json, "my.key.path")
        Assert.assertNotNull(jsonObject)
        Assert.assertEquals("passed", jsonObject.get("test"))
    }
}