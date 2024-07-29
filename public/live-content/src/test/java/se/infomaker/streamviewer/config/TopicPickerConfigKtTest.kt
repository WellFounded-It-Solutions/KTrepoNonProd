package se.infomaker.streamviewer.config

import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test


class TopicPickerConfigKtTest {
    companion object {
        val configJson = "{\n" +
                "\t\"topicsUrl\": \"https://default.json\",\n" +
                "\t\"pickers\": [{\n" +
                "\t\t\t\"type\": \"topic\",\n" +
                "\t\t\t\"title\": \"Title\",\n" +
                "\t\t\t\"config\": {\n" +
                "\t\t\t\t\"url\": \"https://one.json\"\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"topic\",\n" +
                "\t\t\t\"title\": \"Other files\",\n" +
                "\t\t\t\"icon\": \"dude\",\n" +
                "\t\t\t\"config\": {}\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"topic\",\n" +
                "\t\t\t\"title\": \"Other files Uploaded\",\n" +
                "\t\t\t\"icon\": \"dude\",\n" +
                "\t\t\t\"config\": {\n" +   
                "\t\t\t\t\"file\": \"configuration/otherOtherTopics.json\"\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"topic\",\n" +
                "\t\t\t\"title\": \"OLD\",\n" +
                "\t\t\t\"config\": {\n" +
                "\t\t\t\t\"url\": \"https://two.json\"\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"location\",\n" +
                "\t\t\t\"title\": \"Plats\",\n" +
                "\t\t\t\"icon\": \"location_icon\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}"
    }

    @Test
    fun allTopicUrls() {
        val config = Gson().fromJson(configJson, FollowConfig::class.java)
        val topicUrls = config.allTopicUrls()
        Assert.assertTrue(topicUrls.contains("https://default.json"))
        Assert.assertTrue(topicUrls.contains("https://one.json"))
        Assert.assertTrue(topicUrls.contains("https://two.json"))
        Assert.assertEquals(3, topicUrls.size)
    }

    @Test
    fun allTopicNoConfig() {
        val config = FollowConfig()
        val topicUrls = config.allTopicUrls()
        Assert.assertEquals(0, topicUrls.size)
    }
}