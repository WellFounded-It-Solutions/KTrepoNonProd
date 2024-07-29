package se.infomaker.iap.articleview.item.flowplayer

import android.text.format.DateUtils
import se.infomaker.iap.articleview.item.Item
import java.util.UUID

data class FlowPlayerItem(val attributes: Map<String, String>) : Item(attributes["uuid"] ?: UUID.randomUUID().toString()) {

    companion object {
        const val DEFAULT_ASPECT_RATIO = "16:9"
    }
    override val typeIdentifier = FlowPlayerItem::class.java
    override val selectorType = "flowplayer"

    val type = attributes["type"] ?: ""
    val url = attributes["url"] ?: ""
    val thumbnailUrl = attributes["thumbnailUrl"] ?: ""
    val provider = attributes["provider"]
    val mediaType = attributes["mediaType"]
    val publishDate = attributes["publishDate"]
    var start = attributes["start"] ?: ""
    var title = attributes["title"] ?: ""
    var embedCode = attributes["embedCode"]
    var width = attributes["width"]?.toInt()
    var height = attributes["height"]?.toInt()
    var live:Boolean = false
    var autoplay: String? = attributes["autoplay"]
    var mute: String? = attributes["mute"]
    var duration = formatDuration(attributes["duration"])
    var aspectRatio = attributes["aspectRatio"] ?: DEFAULT_ASPECT_RATIO
        get () {
            if (field.isEmpty() || field == "0") {
                return DEFAULT_ASPECT_RATIO
            }
            return field
        }

    var playerId = attributes["playerId"]

    var queryParameters = attributes["queryParameters"] as MutableMap<String, String>?
    private fun formatDuration(duration:String?):String
    {
        var time = duration?.toLong() ?: 0L
        if (mediaType == "livecast") {
            live = true
            return "LIVE"
        }
        return if (time == 0L) "" else DateUtils.formatElapsedTime(time)
    }
    override val matchingQuery = mapOf<String, String>()
}
