package se.infomaker.iap.articleview.item.image

class ImageGalleryItemPreprocessorConfig(
        var baseUrl: String? = null,
        var preferredCrop: String? = null,
        var imageProvider: String = "imengine",
        var fallbackCrop: String? = null,
        var text: String = "none" //text|author|none|all
)
