package se.infomaker.livecontentmanager

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import se.infomaker.livecontentmanager.config.PropertyConfig
import se.infomaker.livecontentmanager.config.TransformSettingsConfig
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser

class PropertyObjectTest {

    @Test
    fun testDescription() {
        val configJson = javaClass.classLoader.getResourceAsStream("propertyTypeMap.json").bufferedReader().use { it.readText() }
        val typeToken = object: TypeToken<Map<String, Map<String, PropertyConfig>>>(){}
        val typePropertyMap = Gson().fromJson<Map<String, Map<String, PropertyConfig>>>(configJson, typeToken.type)

        val typeDescriptionTemplate = mutableMapOf<String, String>()
        typeDescriptionTemplate["Author"] = "{{name}}"
        typeDescriptionTemplate["Article"] = "{{ArticleHeadline}}"
        typeDescriptionTemplate["FollowableConcept"] = "{{name}}"

        val parser = DefaultPropertyObjectParser(typePropertyMap, typeDescriptionTemplate,  TransformSettingsConfig())
        val json = javaClass.classLoader.getResourceAsStream("list.json").bufferedReader().use { it.readText() }
        val list = parser.toPropertyObject(JSONObject(json), "List")
        val optPropertyObjects = list.optPropertyObjects("articles")
        val propertyObject = optPropertyObjects?.get(0)
        val description = propertyObject?.describe()
        Assert.assertNotNull(description)
    }

}