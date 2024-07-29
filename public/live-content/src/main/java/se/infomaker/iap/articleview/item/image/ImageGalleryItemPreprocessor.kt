package se.infomaker.iap.articleview.item.image


import com.google.gson.Gson
import se.infomaker.coremedia.slideshow.ImageObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor

class ImageGalleryItemPreprocessor : Preprocessor {

    private val imageItemPreprocessor = ImageItemPreprocessor()

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val imageConfig = Gson().fromJson(config, ImageItemPreprocessorConfig::class.java)
        imageConfig.baseUrl?.let { base ->
            content.body.items
                    .filterIsInstance<ImageGalleryItem>()
                    .forEach { item ->
                        val list = mutableListOf<ImageObject>()
                        item.images.forEach { image ->
                            imageItemPreprocessor.apply {
                                processImageSizeLimit(image, imageConfig.maxImageSizeLimit)
                                processCrops(image, imageConfig.preferredCrops, imageConfig.preferredCrop, imageConfig.fallbackCrop)
                                processPath(image, base, imageConfig.imageProvider)
                                preparePreloading(image)
                                processText(image, imageConfig.text)
                                updateSlideshowList(image, list, imageConfig.isCroppedInGallery, item.text)
                            }
                        }
                    }
        }
        return content
    }

    private fun updateSlideshowList(
        item: ImageItem,
        slideshowImageList: MutableList<ImageObject>,
        isCroppedInGallery: Boolean,
        captionFallback: String? = null,
    ) {
        item.toImageObjectForSlideshowOrNull(isCroppedInGallery, captionFallback)?.let {
            slideshowImageList.add(it)
            item.slideshowImageList = slideshowImageList
        }
    }

    fun ContentStructure.optString(key: String, fallback: String?): String? {
        return properties.optJSONArray(key)?.optString(0, fallback)
    }
}