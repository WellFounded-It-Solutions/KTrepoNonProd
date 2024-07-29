package se.infomaker.iap.articleview.item.youplay

import android.text.format.DateUtils
import se.infomaker.iap.articleview.item.Item
import timber.log.Timber
import java.util.UUID

data class YouPlayItem(val attributes: Map<String, String>) : Item(attributes["uuid"] ?: UUID.randomUUID().toString()) {

    companion object {
        const val DEFAULT_ASPECT_RATIO = "16:9"
    }
    override val typeIdentifier = YouPlayItem::class.java
    override val selectorType = "youplay"

    var baseUrl:String? = null
    var textToShow = listOf<String>()
    val type = attributes["type"] ?: ""
    var duration:String? = null
        get() {
        if(field.isNullOrEmpty()) {
            try {
                val mins = minutes.toLong()
                val secs = seconds.toLong()
                val time = mins * 60 + secs
                val formattedTime = if (time == 0L) "" else DateUtils.formatElapsedTime(time)
                field = formattedTime
            }
            catch (e: Exception) {
                Timber.e(e, "Could not convert minutes: [$minutes] and seconds: [$seconds] to duration.")
            }
        }
        return field
    }

    var title = attributes["title"] ?: ""
        set(value) {
            field = android.text.Html.fromHtml(value).toString()
        }
    var description:String = attributes["description"] ?: ""
        set(value) {
            field = android.text.Html.fromHtml(value).toString()
        }

    var embedCode = attributes["embedCode"] ?: ""
        set(value) {
            field = "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\"></head><body style=\"margin:0\">$value</body></html>"

            print (field)
        }
    var thumbnailUrl = attributes["thumbnailUrl"] ?: ""
    var width = attributes["width"] ?: 0
    var height = attributes["height"] ?: 0
    var minutes = attributes["minutes"] ?: "00"
    var seconds = attributes["seconds"] ?: "00"

    val url = attributes["url"] ?: ""
    var live:Boolean = false
    var autoplay: String? = attributes["autoplay"]

    var aspectRatio = attributes["aspectRatio"] ?: DEFAULT_ASPECT_RATIO
        get () {
            if (field.isEmpty() || field == "0") {
                return DEFAULT_ASPECT_RATIO
            }
            return field
        }
    override val matchingQuery = mapOf<String, String>()
}
