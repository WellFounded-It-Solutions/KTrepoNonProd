package se.infomaker.livecontentmanager.query

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.config.PropertyConfig


class LiveContentConfigTest {

    @Test
    fun testAlphabeticOrder() {
        val config = LiveContentConfig()
        val typePropertyMap = HashMap<String, Map<String, PropertyConfig>>()
        config.typePropertyMap = typePropertyMap
        typePropertyMap["test"] = modelWith("d", "b", "a", "c")
        val properties = config.getProperties("test")
        assertThat(properties).isEqualTo("a,b,c,d")
    }

    @Test
    fun testMultipleUsesOfSameProperty() {
        val config = LiveContentConfig()
        val typePropertyMap = HashMap<String, Map<String, PropertyConfig>>()
        config.typePropertyMap = typePropertyMap
        val testModel = modelWith("a", "b", "c", "d")
        PropertyConfig("a").let {
            testModel.put("1", it)
        }
        typePropertyMap["test"] = testModel
        val properties = config.getProperties("test")
        assertThat(config.typePropertyMap).containsKey("test")
        assertThat(properties).isEqualTo("a,b,c,d")
    }

    @Test
    fun testSubModels() {
        val config = LiveContentConfig()
        val typePropertyMap = HashMap<String, Map<String, PropertyConfig>>()
        config.typePropertyMap = typePropertyMap
        val testModel = modelWith("a", "c", "d")
        PropertyConfig("b").let {
            it.propertyMapReference = "sub"
            testModel.put("sumbmodel", it)
        }
        typePropertyMap["sub"] = modelWith("s1", "s2", "s3")
        typePropertyMap["test"] = testModel
        val properties = config.getProperties("test")
        assertThat(properties).isEqualTo("a,b[s1,s2,s3],c,d")
    }

    private fun modelWith(vararg args: String): MutableMap<String, PropertyConfig>{
        val model = mutableMapOf<String, PropertyConfig>()
        args.forEach { name ->
            PropertyConfig(name).let {
                model[name] = it
            }
        }
        return model
    }

    @Test
    fun testQuerystreamerLegacyCredentialsFormat() {
        val config = LiveContentConfig()
        config.querystreamerUsername = "user"
        config.querystreamerPassword = "pass"

        assertThat(config.querystreamerUsername).isEqualTo("user")
        assertThat(config.querystreamerPassword).isEqualTo("pass")

        assertThat(config.id).isEqualTo("user")
        assertThat(config.readToken).isEqualTo("pass")

        assertThat(config.querystreamerId).isEqualTo("user")
        assertThat(config.querystreamerReadToken).isEqualTo("pass")
    }

    @Test
    fun testQuerystreamerIntermediateCredentialsFormat() {
        val config = LiveContentConfig()
        config.id = "user"
        config.readToken = "pass"

        assertThat(config.querystreamerUsername).isNull()
        assertThat(config.querystreamerPassword).isNull()

        assertThat(config.id).isEqualTo("user")
        assertThat(config.readToken).isEqualTo("pass")

        assertThat(config.querystreamerId).isEqualTo("user")
        assertThat(config.querystreamerReadToken).isEqualTo("pass")
    }

    @Test
    fun testQuerystreamerNewCredentialsFormat() {
        val config = LiveContentConfig()
        config.querystreamerId = "user"
        config.querystreamerReadToken = "pass"

        assertThat(config.querystreamerUsername).isNull()
        assertThat(config.querystreamerPassword).isNull()

        assertThat(config.id).isNull()
        assertThat(config.readToken).isNull()

        assertThat(config.querystreamerId).isEqualTo("user")
        assertThat(config.querystreamerReadToken).isEqualTo("pass")
    }
}