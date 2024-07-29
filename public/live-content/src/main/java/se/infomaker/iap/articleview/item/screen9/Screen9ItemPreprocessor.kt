package se.infomaker.iap.articleview.item.screen9

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor

class Screen9ItemPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure
    {
        val screen9Config = Gson().fromJson(config, Screen9ItemPreprocessorConfig::class.java)
        var aspectRatio: String? = null
        screen9Config.queryParameters?.forEach {
            when (it.key)
            {
                "aspectRatio" -> {
                    aspectRatio = it.value
                }
            }
        }

        content.body.items
                .filter { item -> item is Screen9Item }
                .forEach {
                    val player = it as Screen9Item
                    player.aspectRatio = aspectRatio ?: player.aspectRatio
                    //player.queryParameters = screen9Config.queryParameters
                }
        return content
    }
}