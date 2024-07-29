package se.infomaker.iap.articleview.item.image

import android.net.Uri

class ImEngineUrlBuilder(val base: Uri, val imageItem: ImageItem, val defaultCropData: CropData? = null) : ImageUrlBuilder {

    var quality: Int? = null

    override fun getFullUri(): Uri? = getBaseBuilder()?.apply {
        if (imageItem.dimensionsExceedMaxSize()) {
            appendQueryParameter("function", "fit")
            appendQueryParameter("maxsize", "${imageItem.maxImageSizeLimit}")
            appendQueryParameter("scaleup", "false")
            return@apply
        }
        appendQueryParameter("function", "original")
    }?.build()

    private val uriBuilder: Uri.Builder?
        get() = imageItem.uri?.let { _ ->
            base.buildUpon()
                .appendPath("imengine")
                .appendPath("image.php")
                ?.apply {
                    appendQueryParameter("type", "preview")
                    appendQueryParameter("source", "false")
                    appendQueryParameter("q", "" + (quality ?: 80))
                    appendQueryParameter("uuid", imageItem.id)
                }
        }

    override fun getBaseUri(): Uri? = uriBuilder?.build()

    private fun getBaseBuilder(): Uri.Builder? = uriBuilder

    override fun getUri(width: Int, height: Int): Uri? =
        (if (defaultCropData != null) getBuilderForCrop(defaultCropData) else getBaseBuilder())?.apply {
            appendQueryParameter("width", width.toString())
            appendQueryParameter("height", height.toString())
        }?.build()

    override fun getUriForCrop(cropData: CropData): Uri? = getBuilderForCrop(cropData)?.build()

    private fun getBuilderForCrop(cropData: CropData): Uri.Builder? = uriBuilder?.apply {
        appendQueryParameter("function", "cropresize")
        appendQueryParameter("crop_w", cropData.width.toString())
        appendQueryParameter("crop_h", cropData.height.toString())
        appendQueryParameter("x", cropData.x.toString())
        appendQueryParameter("y", cropData.y.toString())
    }
}