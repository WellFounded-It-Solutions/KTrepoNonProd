package se.infomaker.iap.articleview.item.flowplayer

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor

class FlowPlayerItemPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure
    {
        val flowPlayerConfig = Gson().fromJson(config, FlowPlayerItemPreprocessorConfig::class.java)
        var playerId: String? = null
        flowPlayerConfig.queryParameters?.forEach shortReturn@{
            when (it.key)
            {
                "pi" -> {
                    playerId = it.value
                    return@shortReturn
                }
            }
        }

        content.body.items
                .filter { item -> item is FlowPlayerItem }
                .forEach {
                    var player = it as FlowPlayerItem

                    if (player.playerId == "undefined") {
                        player.playerId = null
                    }
                    player.playerId = playerId ?: player.playerId
                    flowPlayerConfig.queryParameters?.remove("pi")
                    player.autoplay = flowPlayerConfig.queryParameters?.get("autoplay") ?: player.autoplay
                    flowPlayerConfig.queryParameters?.remove("autoplay")
                    player.start    = flowPlayerConfig.queryParameters?.get("start") ?: player.start
                    flowPlayerConfig.queryParameters?.remove("start")
                    player.mute    = flowPlayerConfig.queryParameters?.get("mute") ?: player.mute
                    flowPlayerConfig.queryParameters?.remove("mute")
                    player.queryParameters = flowPlayerConfig.queryParameters
                }
        return content
    }
}