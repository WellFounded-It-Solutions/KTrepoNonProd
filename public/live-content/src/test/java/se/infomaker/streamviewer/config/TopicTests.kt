package se.infomaker.streamviewer.config

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import se.infomaker.streamviewer.tabs.TopicPickerHelper
import java.io.IOException


class TopicTester {

    companion object {
        val withData = "{\n" +
                "\t\t\t\t\"url\": \"https://s3-eu-west-1.amazonaws.com/iap-simple-config/infomaker/test/otherOtherTopics.json\",\n" +
                "\t\t\t\t\"file\": \"aaaconfiguration/topics.json\"\n" +
                "}"

        val withOnlyUrl = "{\n" +
                "\t\t\t\t\"url\": \"https://s3-eu-west-1.amazonaws.com/iap-simple-config/infomaker/test/otherOtherTopics.json\"\n" +
                "}"

        val withOnlyFile = "{\n" +
                "\t\t\t\t\"file\": \"configuration/topics.json\"\n" +
                "}"
 
        val withEmptyData = "{\n" +
                "\t\t\t\t\"url\": \"\",\n" +
                "\t\t\t\t\"file\": \"\"\n" +
                "}"
    }


    @Test
    fun withEmptyData() {
        setupTestData(withEmptyData)[0].apply {
            assertEquals("topic", type)
            assertEquals("Ämne", title)
            assertEquals("dude", icon)
            val topicPickerConfig = Gson().fromJson(config, TopicPickerConfig::class.java)
            assertEquals("", topicPickerConfig.url)
            assertEquals("", topicPickerConfig.file)
            Assert.assertNotEquals("https/weird.com", topicPickerConfig.file)
        }
    }

    @Test
    fun withoutConfig() {

        val testResourceManager = TestResourceManager()
        val nearMeConfig = FollowConfig()
        nearMeConfig.topicsUrl = "BackupOnlyFile"

        testResourceManager.getPickers(nearMeConfig)[0].apply {
            assertEquals("topic", type)
            assertEquals(null, title)
            assertEquals(null, icon)

            val topicPickerConfig = Gson().fromJson(config, TopicPickerConfig::class.java)
            assertEquals("BackupOnlyFile", topicPickerConfig.url)
            assertEquals("shared/configuration/topics.json", topicPickerConfig.file)
            Assert.assertNotEquals("https/doge.com", topicPickerConfig.file)
        }
    }

    @Test
    fun withoutAnyData() {
        val testResourceManager = TestResourceManager()
        val nearMeConfig = FollowConfig()
        nearMeConfig.topicsUrl = "BackupOnlyFile"

        testResourceManager.getPickers(nearMeConfig)[0].apply {
            assertEquals("topic", type)
            assertEquals(null, title)
            assertEquals(null, icon)

            val topicPickerConfig = Gson().fromJson(config, TopicPickerConfig::class.java)
            assertEquals("BackupOnlyFile", topicPickerConfig.url)
            assertEquals("shared/configuration/topics.json", topicPickerConfig.file)
            Assert.assertNotEquals("https/doge.com", topicPickerConfig.file)
        }
    }

    @Test
    fun urlAndFile() {

        setupTestData(withData)[0].apply {
            assertEquals("topic", type)
            assertEquals("Ämne", title)
            assertEquals("dude", icon)
            val topicPickerConfig = Gson().fromJson(config, TopicPickerConfig::class.java)
            assertEquals("https://s3-eu-west-1.amazonaws.com/iap-simple-config/infomaker/test/otherOtherTopics.json", topicPickerConfig.url)
            assertEquals("aaaconfiguration/topics.json", topicPickerConfig.file)
            Assert.assertNotEquals("https/doge.com", topicPickerConfig.file)
        }
    }

    @Test
    fun onlyUrl() {
        setupTestData(withOnlyUrl)[0].apply {
            assertEquals("topic", type)
            assertEquals("Ämne", title)
            assertEquals("dude", icon)
            val topicPickerConfig = Gson().fromJson(config, TopicPickerConfig::class.java)
            assertEquals("https://s3-eu-west-1.amazonaws.com/iap-simple-config/infomaker/test/otherOtherTopics.json", topicPickerConfig.url)
            assertEquals("shared/configuration/topics.json", topicPickerConfig.file)
            assertNotEquals("https/delicious.com", topicPickerConfig.file)
        }
    }

    @Test
    fun onlyFile() {

        setupTestData(withOnlyFile)[0].apply {
            assertEquals("topic", type)
            assertEquals("Ämne", title)
            assertEquals("dude", icon)
            val topicPickerConfig = Gson().fromJson(config, TopicPickerConfig::class.java)
            assertEquals(null, topicPickerConfig.url)
            assertEquals("configuration/topics.json", topicPickerConfig.file)
            Assert.assertNotEquals("https/swag.com", topicPickerConfig.file)
        }
    }


    @Test
    fun locationPicker() {
        val testResourceManager = TestResourceManager()
        val nearMeConfig = FollowConfig()

        val picker = PickerConfig("location", "Plats", "dude", null)
        nearMeConfig.pickers.add(picker)
        val locationPicker = testResourceManager.getPickers(nearMeConfig)
        assertEquals("location", locationPicker[0].type)
        assertEquals("Plats", locationPicker[0].title)


    }

    private fun setupTestData(jsonStringToTest: String): List<PickerConfig> {
        val testResourceManager = TestResourceManager()
        val nearMeConfig = FollowConfig()

        val jsonObject: JsonObject? = Gson().fromJson(jsonStringToTest, JsonObject::class.java)
        val picker = PickerConfig("topic", "Ämne", "dude", jsonObject)

        nearMeConfig.topicsUrl = "BackupOnlyFile"
        nearMeConfig.pickers.add(picker)

        return testResourceManager.getPickers(nearMeConfig)
    }

    @Test
    fun resourceManagerReturnResult() {

        val nearMeConfig = FollowConfig()

        val jsonWithOnlyFile: JsonObject? = Gson().fromJson(withOnlyFile, JsonObject::class.java)
        val pickerWithOnlyFile = PickerConfig("topic", "Ämne", "dude", jsonWithOnlyFile)

        nearMeConfig.topicsUrl = "BackupOnlyFile"
        nearMeConfig.pickers.add(pickerWithOnlyFile)

        val testResourceManager = mock(TestResourceManager::class.java)
        `when`(testResourceManager.getPickers(nearMeConfig)).thenReturn(nearMeConfig.pickers)
        assertEquals(nearMeConfig.pickers.size, 1)
        assertEquals(nearMeConfig.pickers[0].title, "Ämne")

    }
}


open class TestResourceManager {

    fun getPickers(config: FollowConfig?): List<PickerConfig> {
        val gson = Gson()

        if (config != null && config.pickers.isNotEmpty()) {
            return config.pickers.map {
                if (it.type != "topic") {
                    return@map it
                }
                val sourceConfig = gson.fromJson(it.config, TopicPickerConfig::class.java)

                if (sourceConfig == null) {
                    it.config = gson.toJsonTree(TopicPickerConfig(config.topicsUrl, TopicPickerHelper.DEFAULT_TOPICS_FILE)) as JsonObject
                } else {
                    it.config = gson.toJsonTree(TopicPickerConfig(sourceConfig.url, sourceConfig.file
                            ?: TopicPickerHelper.DEFAULT_TOPICS_FILE
                            ?: config.topicsUrl)) as JsonObject
                }
                return@map it
            }
        } else {
            val output = mutableListOf<PickerConfig>()
            try {
                val pickerConfig = gson.toJsonTree(TopicPickerConfig(config?.topicsUrl, TopicPickerHelper.DEFAULT_TOPICS_FILE)) as JsonObject
                output.add(PickerConfig("topic", null, null, pickerConfig))

            } catch (ignore: IOException) {
                // We should not add any picker if the default topics file does not exist
            }
            return output
        }
    }
}