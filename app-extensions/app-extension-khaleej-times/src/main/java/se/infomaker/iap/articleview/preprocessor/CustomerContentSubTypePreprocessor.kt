package se.infomaker.iap.articleview.preprocessor

import se.infomaker.frtutilities.JSONUtil
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.embed.HtmlEmbedItem
import java.util.UUID

class CustomerContentSubTypePreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        when (JSONUtil.optJSONArray(content.properties, "customerContentSubType")?.getString(0)) {
            "VIDEO" -> {
                content.properties.optJSONArray("customerVideoId")?.getString(0)?.let { customerVideoId ->
                    val embeddedVideo = embedVideoId(customerVideoId)
                    val htmlEmbed = HtmlEmbedItem.builder {
                        uuid = UUID.randomUUID().toString()
                        html = embeddedVideo
                    }.build()
                    content.body.items.add(htmlEmbed)
                }
            }
        }
        return content
    }

    companion object {
        fun embedVideoId(videoId: String): String = "<div style=\"position:relative;padding-bottom:56.25%;height:0;overflow:hidden;\"> <iframe style=\"width:100%;height:100%;position:absolute;left:0px;top:0px;overflow:hidden\" frameborder=\"0\" type=\"text/html\" src=\"https://www.dailymotion.com/embed/video/$videoId?autoplay=1\" width=\"100%\" height=\"100%\" allowfullscreen allow=\"autoplay\"> </iframe> </div>"
    }
}