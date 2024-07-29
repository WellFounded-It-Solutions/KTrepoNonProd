package se.infomaker.iap.articleview.offline

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.image.ImageItem
import timber.log.Timber
import java.io.File
import java.lang.Exception

class ImageItemLoader : ItemLoader<ImageItem> {

    override suspend fun loadItem(context: Context, item: ImageItem, resourceManager: ResourceManager) {
        // The Cropped version of the image
        load(context, item.preloadUri(context))
        // The slideshow version of the image
        load(context, item.urlBuilder?.getFullUri())
    }

    private fun load(context: Context, uri: Uri?) {
        uri?.let {
            try {
                Glide.with(context).downloadOnly().load(it).listener(object : RequestListener<File>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File>?, isFirstResource: Boolean): Boolean {
                        Timber.w("Failed to cache image $uri")
                        return true
                    }

                    override fun onResourceReady(resource: File?, model: Any?, target: Target<File>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Timber.d("Cached image: $uri")
                        return true
                    }
                }).submit().get()
            } catch (e: Exception) {
                Timber.w(e,"Failed to fetch $uri")
            }
        }
    }
}
