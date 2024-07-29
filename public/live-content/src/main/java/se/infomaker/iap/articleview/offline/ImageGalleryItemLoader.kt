package se.infomaker.iap.articleview.offline

import android.content.Context
import kotlinx.coroutines.runBlocking
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.image.ImageGalleryItem
import se.infomaker.iap.articleview.item.image.ImageItem

class ImageGalleryItemLoader : ItemLoader<ImageGalleryItem> {

    private val imageLoader = ImageItemLoader()

    override suspend fun loadItem(context: Context, item: ImageGalleryItem, resourceManager: ResourceManager) {
        runBlocking {
            item.images.mapNotNull { it as? ImageItem }.forEach { item ->
                imageLoader.loadItem(context, item, resourceManager)
            }
        }
    }
}
