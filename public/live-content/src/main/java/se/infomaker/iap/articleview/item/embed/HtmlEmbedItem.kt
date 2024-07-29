package se.infomaker.iap.articleview.item.embed

import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.RequiresNetwork
import java.util.UUID

data class HtmlEmbedItem(val id: String, val type: String, var data: String) : Item(id), RequiresNetwork {

    override val typeIdentifier = HtmlEmbedItem::class.java
    override val matchingQuery = mapOf<String, String>()
    override val selectorType = "htmlEmbed"

    var baseUrl:String? = null
    var size:String? = null
    var width:Int = 0
    var height:Int = 0
    var scheme:String? = null
    var linkText:String? = null
    var linkType:String? = "internalLink"
    var contentBasedAspectRatio:Double = 0.0
    var containsIframe: Boolean = false
    var src: String? = null
    var webViewPool:WebViewRecyclerPool? = null

    companion object {
        const val DEFAULT_ASPECT_RATIO = "16:9"
        fun create(init: Builder.() -> Unit) = Builder(init).build()
        fun builder(init: Builder.() -> Unit) = Builder(init)
        fun builder() = Builder()
    }

    class Builder() {
        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        var type: String = "default"
        var uuid: String = UUID.randomUUID().toString()
        var html: String = ""

        fun build() = HtmlEmbedItem(
                id = uuid,
                type = type,
                data = "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\"></head><body style=\"margin:0\">$html</body></html>")
    }

    fun heightForWidth(width: Int, aspectRatio: String): Int {
        val parts = aspectRatio.split(":")
        val ratio = when (parts.size) {
            1 -> parts[0].toDouble()
            2 -> {
                val widthPart = parts[0].toDouble()
                val heightPart = parts[1].toDouble()
                widthPart / heightPart
            }
            else -> throw IllegalArgumentException("AspectRatio is invalid, size=$aspectRatio")
        }
        return (width / ratio).toInt()
    }
}
