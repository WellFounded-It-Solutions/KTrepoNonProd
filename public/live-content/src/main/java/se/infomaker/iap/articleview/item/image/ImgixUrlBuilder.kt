package se.infomaker.iap.articleview.item.image

import android.net.Uri

class ImgixUrlBuilder(val base: Uri, val imageItem: ImageItem, val defaultCropData: CropData? = null) : ImageUrlBuilder {
    override fun getFullUri(): Uri? {
        return getBaseUri()
    }

    private val uriBuilder: Uri.Builder?
        get() {
            return imageItem.uri?.let {
                base.buildUpon().appendPath(Uri.parse(it).lastPathSegment)
            }
        }

    override fun getBaseUri(): Uri? {
        return uriBuilder?.build()
    }

    override fun getUri(width: Int, height: Int): Uri? {
        return if (defaultCropData != null) getUriForCrop(defaultCropData) else getBaseUri()
    }

    override fun getUriForCrop(cropData: CropData): Uri? {
        val cropDataPixels = cropData * imageItem
        return uriBuilder?.appendQueryParameter("rect",
                "${cropDataPixels.x.toInt()},${cropDataPixels.y.toInt()},${cropDataPixels.width.toInt()},${cropDataPixels.height.toInt()}")?.build()
    }
}