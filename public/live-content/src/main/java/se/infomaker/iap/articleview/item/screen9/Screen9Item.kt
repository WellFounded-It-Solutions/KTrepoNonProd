package se.infomaker.iap.articleview.item.screen9

import android.text.format.DateUtils
import se.infomaker.iap.articleview.item.Item
import java.util.UUID

data class Screen9Item(val attributes: Map<String, String>, val data: Map<String, String>) : Item(attributes["uuid"] ?: UUID.randomUUID().toString()) {
    override val matchingQuery = mapOf<String, String>()
    override val typeIdentifier = Screen9Item::class.java
    override val selectorType = "screen9"

    companion object {
        fun formatDuration(duration:Int?):String
        {
            val time = duration?.toLong() ?: 0L
            return if (time == 0L) "" else DateUtils.formatElapsedTime(time)
        }

        const val DEFAULT_ASPECT_RATIO = "16:9"
    }

    val height: Int
        get() = data["height"]?.trim()?.toInt() ?: 0

    val width: Int
        get() = data["width"]?.trim()?.toInt() ?: 0

    val videoUrl: String?
        get() {
            data["src"]?.trim()?.let {
                if (it.startsWith("http")) {
                    return it
                }
                return "https:$it"
            }
            return null
        }
    val mediaid: String?
        get() = data["mediaid"]?.trim()

    val html: String?
        get() = data["html"]?.trim()

    val accountid: String?
        get() = data["accountid"]?.trim()

    val title: String?
        get() = data["title"]?.trim()

    val description: String?
        get() = data["description"]?.trim()

    val providerName: String?
        get() = data["provider_name"]?.trim()

    var aspectRatio = attributes["aspectRatio"] ?: DEFAULT_ASPECT_RATIO
        get () {
            if (field.isEmpty() || field == "0") {
                return DEFAULT_ASPECT_RATIO
            }
            return field
        }

    val duration: Int
        get() {
            data["duration"]?.trim()?.let {
                try {
                    return Math.round(it.toDouble()).toInt()
                }
                catch (e: NumberFormatException) {

                }
            }
            return 0
        }

    val thumbnailUrl: String?
        get() = data["thumbnail_url"]?.trim()
}