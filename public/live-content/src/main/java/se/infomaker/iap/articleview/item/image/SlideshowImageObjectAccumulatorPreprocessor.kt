package se.infomaker.iap.articleview.item.image

import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor

class SlideshowImageObjectAccumulatorPreprocessor : Preprocessor {

    override fun process(
        content: ContentStructure,
        config: String,
        resourceProvider: ResourceProvider,
    ): ContentStructure {
        // Get all ImageItems
        val imageItems: List<ImageItem> = content.body.items.filterIsInstance<ImageItem>()

        // Build list of ImageObjects
        val imageObjects = imageItems.mapNotNull { it.toImageObjectForSlideshowOrNull(false) }.toMutableList()

        // Update each ImageItem's list of ImageObjects
        content.body.items.filterIsInstance<ImageItem>().map {
            it.slideshowImageList = imageObjects
        }
        return content
    }
}