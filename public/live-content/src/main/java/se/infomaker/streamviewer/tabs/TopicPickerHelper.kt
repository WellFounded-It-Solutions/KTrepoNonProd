package se.infomaker.streamviewer.tabs

import com.google.gson.Gson
import com.google.gson.JsonObject
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.config.PickerConfig
import se.infomaker.streamviewer.config.TopicPickerConfig
import java.io.IOException


class TopicPickerHelper {
    companion object {
        const val DEFAULT_TOPICS_FILE: String = "shared/configuration/topics.json"
    }

    fun getPickers(config: FollowConfig?): List<PickerConfig> {
        val gson = Gson()

        if (config != null && config.pickers.isNotEmpty()) {
            return config.pickers.mapNotNull {
                if (it.type != "topic") {
                    return@mapNotNull null
                }
                val sourceConfig = gson.fromJson(it.config, TopicPickerConfig::class.java)

                if (sourceConfig == null) {
                    it.config = gson.toJsonTree(TopicPickerConfig(config.topicsUrl, DEFAULT_TOPICS_FILE)) as JsonObject
                } else {
                    it.config = gson.toJsonTree(TopicPickerConfig(
                            sourceConfig.url ?: config.topicsUrl,
                            sourceConfig.file ?: DEFAULT_TOPICS_FILE)
                    ) as JsonObject
                }
                return@mapNotNull it
            }
        } else {
            val output = mutableListOf<PickerConfig>()
            try {
                val pickerConfig = gson.toJsonTree(TopicPickerConfig(config?.topicsUrl, DEFAULT_TOPICS_FILE)) as JsonObject
                output.add(PickerConfig("topic", null, null, pickerConfig))

            } catch (ignore: IOException) {
                // We should not add any picker if the default topics file does not exist
            }
            return output
        }
    }
}