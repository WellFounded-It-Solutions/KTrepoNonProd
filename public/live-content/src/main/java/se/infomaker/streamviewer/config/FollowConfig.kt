package se.infomaker.streamviewer.config

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import se.infomaker.livecontentui.config.LiveContentUIConfig

data class FollowConfig(
    @SerializedName("remoteNotification") private val _remoteNotification: RemoteNotificationConfig? = null,
    @SerializedName("enablePushOnSubscription") private val _enablePushOnSubscription: Boolean? = null
) : LiveContentUIConfig() {
    val remoteNotification: RemoteNotificationConfig
        get() = _remoteNotification ?: RemoteNotificationConfig()
    val enablePushOnSubscription: Boolean
        get() = _enablePushOnSubscription ?: true
    val pickers = mutableListOf<PickerConfig>()
    @Deprecated(
        "Only tabs are supported. No need to specify a layout. Will always return \"tabs\".",
        replaceWith = ReplaceWith("Nothing.")
    )
    val layout: String
        get() = "tabs"
    var topicsUrl: String? = null
    var prominentTopicPickerButtons: Boolean? = false
    val cleanSubscriptionStorageIdentifier: String? = null
    var topicPickerAlwaysShowSearchbar: Boolean? = false

    /**
     * @returns a set of all urls used by configured topic pickers
     */
    fun allTopicUrls(): Set<String> {
        return pickers.filter {
            it.type == "topic"
        }.mapNotNull { pickerConfig ->
            Gson().fromJson(pickerConfig.config, TopicPickerConfig::class.java)
        }.mapNotNull { topicConfig ->
            topicConfig.url ?: topicsUrl
        }.toSet()
    }
}