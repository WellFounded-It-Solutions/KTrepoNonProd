package se.infomaker.frtutilities

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import org.junit.Test
import java.util.HashMap

class MapUtilKtTest {
    val before: Map<Any, Any>
    val toMerge: Map<Any, Any>
    val expectedResult: Map<Any, Any>

    val beforeString = """
        {
          "cutme": "please",
          "first": "firstValue",
          "second": {
            "hello": {
              "change": "this",
              "doNotChange": "this"
            },
            "hi": {
              "this": "value"
            }
          }
        }
        """

    val toMergeString = """
    {
      "cutme": null,
      "second": {
        "hello": {
          "change": "that"
          },
          "hi": "there"
        },
      "thisIs": "new"
    }
    """

    val expectedResultString = """
    {
      "first": "firstValue",
      "second": {
        "hello": {
          "change": "that",
          "doNotChange": "this"
        },
        "hi": "there"
      },
      "thisIs": "new"
    }
    """

    val gson = GsonBuilder().create()

    init {
        before = gson.fromJson<Map<Any, Any>>(beforeString, HashMap::class.java)
        toMerge = gson.fromJson<Map<Any, Any>>(toMergeString, HashMap::class.java)
        expectedResult = gson.fromJson<Map<Any, Any>>(expectedResultString, HashMap::class.java)
    }

    @Test
    fun putRecursive() {
        val mutableMap = mutableMapOf<Any, Any>()
        mutableMap.putAll(before)

        mutableMap.putRecursive(toMerge)
        val jsonParser = JsonParser()
        val mapElement = jsonParser.parse(gson.toJson(mutableMap).toString())
        val expectedResultElement = jsonParser.parse(gson.toJson(expectedResult).toString())
        assertEquals(mapElement, expectedResultElement)
        assertFalse(mutableMap.containsKey("cutme"))
    }
}