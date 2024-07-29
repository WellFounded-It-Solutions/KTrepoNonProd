package se.infomaker.iap.articleview.item.embed

class HtmlEmbedItemPreprocessorConfig {
    var baseUrl: String? = null
    var aspectRatio: String? = HtmlEmbedItem.DEFAULT_ASPECT_RATIO
    var embed: List<EmbedConfig>? = listOf()
}
