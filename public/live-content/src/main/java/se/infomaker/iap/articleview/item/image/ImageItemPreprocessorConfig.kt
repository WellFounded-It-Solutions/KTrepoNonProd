package se.infomaker.iap.articleview.item.image

class ImageItemPreprocessorConfig(
        var baseUrl: String? = null,
        var preferredCrops: List<String>? = null,
        var imageProvider: String = "imengine",
        var isCroppedInGallery:Boolean = false,
        var fallbackCrop: String? = null,
        var text: String = "none", //text|author|none|all
        @Deprecated("Use preferredCrops instead")
        var preferredCrop: String? = null,
        var maxImageSizeLimit: Int? = null,
)
