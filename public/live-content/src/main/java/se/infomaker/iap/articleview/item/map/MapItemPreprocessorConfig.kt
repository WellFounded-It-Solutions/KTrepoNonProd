package se.infomaker.iap.articleview.item.map

class MapItemPreprocessorConfig {
    var interaction: String = INTERACTIVE
    var aspectRatio: String = MapItemViewFactory.DEFAULT_ASPECT_RATIO

    companion object {
        const val STATIC = "static"
        const val INTERACTIVE = "interactive"
        const val EXTERNAL = "external"
    }
}
