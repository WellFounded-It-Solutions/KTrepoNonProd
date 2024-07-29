package se.infomaker.iap.articleview.offline

import android.content.Context
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item

/**
 * Convenience interface to get items of correct type when implementing loader
 */
interface ItemLoader<T : Item> {
    suspend fun load(context: Context, item: Item, resourceManager: ResourceManager) {
        (item as? T)?.let {
            loadItem(context, it, resourceManager)
        }
    }

    /**
     * Load and cache item
     */
    suspend fun loadItem(context: Context, item: T, resourceManager: ResourceManager)
}
