package se.infomaker.iap.articleview.view

import android.content.Context
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.livecontentui.config.LiveContentUIConfig
import se.infomaker.livecontentui.livecontentrecyclerview.binder.BinderCollection
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMFrameLayoutBinder
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMImageViewBinder
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMTextViewBinder
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlFactoryProvider
import timber.log.Timber

object BinderProvider {
    private val binders = mutableMapOf<String, PropertyBinder>()

    fun binder(context: Context, moduleId: String): PropertyBinder {
        binders[moduleId]?.let {
            return it
        }
        synchronized(this) {
            binders[moduleId]?.let {
                return it
            }
            val binder = create(context, moduleId)
            binders[moduleId] = binder
            return binder
        }
    }

    private fun create(context: Context, moduleId: String): PropertyBinder {
        val config = ConfigManager.getInstance(context).getConfig(moduleId, LiveContentUIConfig::class.java)
        val imageUrlBuilderFactory = ImageUrlFactoryProvider().provide(config.imageProvider, config.imageBaseUrl)

        var imageSizes: List<Double>? = null
        try {
            imageSizes = config.media.image.sizes
        } catch (e: Exception) {
            Timber.e(e)
        }

        return PropertyBinder(BinderCollection.with(IMImageViewBinder(imageUrlBuilderFactory, imageSizes), IMTextViewBinder(ResourceManager(context, moduleId)), IMFrameLayoutBinder()))
    }
}