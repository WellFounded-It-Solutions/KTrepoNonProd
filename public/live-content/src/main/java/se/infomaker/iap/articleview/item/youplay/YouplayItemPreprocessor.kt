package se.infomaker.iap.articleview.item.youplay

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor

class YouplayItemPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure
    {
        val youplayConfig = Gson().fromJson(config, YouplayItemPreprocessorConfig::class.java)

        youplayConfig.baseUrl?.let { baseUrl ->
            content.body.items
                    .filter { item -> item is YouPlayItem }
                    .forEach {
                        val player = it as YouPlayItem

                        player.baseUrl = baseUrl
                        player.textToShow = youplayConfig.text.split("|").distinct()
                    }
        }
        return content
    }
}