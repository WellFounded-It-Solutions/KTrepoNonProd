package se.infomaker.coremedia.slideshow

import android.content.Context
import se.infomaker.coremedia.CoreMediaObject
import se.infomaker.coremedia.CoreMediaPlayer
import timber.log.Timber

class SlideshowCoreMediaPlayer : CoreMediaPlayer {
    companion object {
        val NAME = "slideshow"
    }

    var coreMediaObject: CoreMediaObject? = null
    var color: Int = 0
    var config: Map<String, Any> = mutableMapOf()

    override fun getName(): String? {
        return NAME
    }

    override fun init(coreMediaObject: CoreMediaObject?, color: Int, config: Map<String, Any>?) {
        Timber.d("init()")
        this.coreMediaObject = coreMediaObject
        this.color = color
        if (config != null)
            this.config = config
    }

    override fun start(context: Context) {
        Timber.d("start()")

        val images = (coreMediaObject!!.attributes!!["images"] as List<Map<String, String>>).map {
            val url = it["url"]
            if (url != null) {
                ImageObject(url, it["cropUrl"], it["description"], it["photographers"] as List<String>)
            } else {
                null
            }
        }.filterNotNull()


        val intent = SlideshowActivity.createIntent(
                context = context,
                currentImage = coreMediaObject!!.attributes!!["currentImage"] as String,
                images = images,
                captionFont = config["captionFont"] as? String,
                captionFontSize = config["captionFontSize"] as? Double)

        intent.putExtra(SlideshowActivity.ARG_MODULE_ID, coreMediaObject!!.attributes!!["moduleId"] as String)
        intent.putExtra(SlideshowActivity.ARG_MODULE_NAME, coreMediaObject!!.attributes!!["moduleName"] as String)
        intent.putExtra(SlideshowActivity.ARG_MODULE_TITLE, coreMediaObject!!.attributes!!["moduleTitle"] as String)
        intent.putExtra(SlideshowActivity.ARG_ARTICLE_HEADLINE, coreMediaObject!!.attributes!!["articleHeadline"] as String)
        intent.putExtra(SlideshowActivity.ARG_ARTICLE_UUID, coreMediaObject!!.attributes!!["articleUuid"] as String)

        context.startActivity(intent)
    }
}