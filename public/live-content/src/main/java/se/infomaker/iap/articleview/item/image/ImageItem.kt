package se.infomaker.iap.articleview.item.image

import android.content.Context
import android.graphics.Point
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import se.infomaker.coremedia.slideshow.ImageObject
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.author.AuthorItem
import se.infomaker.iap.articleview.item.element.ElementItem
import java.math.RoundingMode
import java.util.UUID


data class ImageItem(
    val id: String,
    val type: String,
    val uri: String?,
    val width: Int,
    val height: Int,
    val text: String,
    val alttext: String,
    val crops: Map<String, String>,
    var authors: MutableList<AuthorItem>,
    val textElement: ElementItem? = null,
    var slideshowExtras: Bundle? = null,
    override val selectorType: String = "image",
    var contentPartSource: Boolean = false,
    var placeholderImage: Drawable? = null,
    var urlBuilder: ImageUrlBuilder? = null,
    var cropDatas: MutableMap<String, CropData> = mutableMapOf(),
    var defaultCrop: CropData? = null,
    var slideshowImageList: MutableList<ImageObject> = mutableListOf(),
    override val matchingQuery: Map<String, String> = mapOf(),
    var textToShow: List<String> = listOf(),
    val disableAutomaticCrop: Boolean = false,
) : Item(id) {

    override val typeIdentifier = ImageItem::class.java
    val themeKey: String = type
    var themeKeys: List<String> = listOf(type, selectorType)
    var maxImageSizeLimit = MAX_ALLOWED_SIZE

    private val slideshowAuthorList: List<String>
        get() = authors.mapNotNull { it.fields.firstOrNull()?.content }

    val authorListAsStringOrNull: String?
        get() {
            if ((textToShow.contains(TEXTS_ALL) || textToShow.contains(TEXTS_AUTHOR)) && authors.isNotEmpty()) {
                return authors.mapNotNull { it.fields.firstOrNull()?.content }.joinToString(", ")
            }
            return null
        }

    val imageDimensionsWithCrop: ImageDimensions
        get() {
            val crop = defaultCrop ?: NO_CROP
            val (scaledWidth, scaledHeight) = adjustDimensionsForOversizedImages(width.toDouble(), height.toDouble())
            return ImageDimensions(width = scaledWidth * crop.width, height = scaledHeight * crop.height)
        }

    val descriptionSpannableStringBuilder: SpannableStringBuilder?
        get() {
            textElement?.let {
                if (textToShow.contains(TEXTS_ALL) || textToShow.contains(TEXTS_TEXT)) {
                    return it.text
                }
            }
            return null
        }

    val aspectRatio: String
        get() = imageDimensionsWithCrop.aspectRatio

    fun dimensionsExceedMaxSize(): Boolean = width > maxImageSizeLimit || height > maxImageSizeLimit

    fun preloadUri(context: Context): Uri? = sizedUri(context.resources.displayMetrics.widthPixels)

    fun previewUri(): Uri? = sizedUri(100)

    fun imageDimensionsForScaledWidth(width: Double): ImageDimensions {
        if (imageDimensionsWithCrop.invalidDimensions()) return imageDimensionsWithCrop
        val scaledHeight = (imageDimensionsWithCrop.height * (width / imageDimensionsWithCrop.width)).round()
        return ImageDimensions(width, scaledHeight)
    }

    fun Double.round(): Double = this.toBigDecimal().setScale(0, RoundingMode.HALF_UP).toDouble()

    private fun sizedUri(imageWidth: Int): Uri? {
        val dimensions = imageDimensionsForScaledWidth(imageWidth.toDouble())
        return urlBuilder?.getUri(imageWidth, dimensions.height.toInt())
    }

    /**
     * Creates an [ImageObject] for the slideshow or null if the [ImageItem]'s [ImageUrlBuilder]
     * is null.
     *
     * @param isCroppedInGallery
     * @param captionFallback
     * @param maxImageSizeLimit
     * @return [ImageObject]?
     */
    fun toImageObjectForSlideshowOrNull(
        isCroppedInGallery: Boolean = false,
        captionFallback: String? = null,
    ): ImageObject? {
        urlBuilder?.let { builder ->

            val (width, height) = if (isCroppedInGallery) {
                imageDimensionsWithCrop
            } else {
                adjustDimensionsForOversizedImages(
                width = width.toDouble(),
                height = height.toDouble())
            }

            return ImageObject(
                url = builder.getFullUri().toString(),
                cropUrl = slideShowCropUrlOrNull(
                    builder = builder,
                    croppedImageWidth = width,
                    croppedImageHeight = height,
                    isCroppedInGallery = isCroppedInGallery),
                description = text.ifBlank { captionFallback ?: "" },
                photographers = slideshowAuthorList,
                size = Point(width.toInt(), height.toInt()),
                uuid = id)
        }
        return null
    }

    private fun cropOrNull(
        builder: ImageUrlBuilder?,
        croppedImageWidth: Double,
        croppedImageHeight: Double,
    ): String = builder?.getUri(croppedImageWidth.toInt(), croppedImageHeight.toInt()).toString()

    private fun slideShowCropUrlOrNull(
        builder: ImageUrlBuilder,
        croppedImageWidth: Double,
        croppedImageHeight: Double,
        isCroppedInGallery: Boolean = false,
    ): String? {
        if (!isCroppedInGallery) return null
        return cropOrNull(builder, croppedImageWidth, croppedImageHeight)
    }

    /**
     * Checks the image dimensions and, if required, adjusts the image to the maxImageSizeLimit
     * or returns them as is without modification.
     *
     * @param width
     * @param height
     *
     * @return the correct image dimensions
     */
    private fun adjustDimensionsForOversizedImages(
        width: Double,
        height: Double,
    ): ImageDimensions {
        if (invalidDimensions(width, height)) return ImageDimensions(width, height)
        if (!dimensionsExceedMaxSize()) return ImageDimensions(width, height)
        return if (width >= height) {
            val aspectRatio = width / height
            ImageDimensions(maxImageSizeLimit, (maxImageSizeLimit / aspectRatio).round())
        } else {
            val aspectRatio = height / width
            ImageDimensions((maxImageSizeLimit / aspectRatio).round(), maxImageSizeLimit)
        }
    }

    private fun invalidDimensions(width: Double, height: Double): Boolean = width == 0.0 || height == 0.0

    private fun ImageDimensions.invalidDimensions(): Boolean = invalidDimensions(this.width, this.height)

    data class ImageDimensions(val width: Double, val height: Double) {
        val aspectRatio: String
            get() = "${width.toInt()}:${height.toInt()}"
    }

    fun getDescription(): String? {
        if (text.isEmpty()) {
            return null
        }
        if (textToShow.contains(TEXTS_ALL) || textToShow.contains(TEXTS_TEXT)) {
            return text
        }
        return null
    }

    class Builder() {
        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        var type: String = "default"
        var uuid: String? = null
        var uri: String? = null
        var width: Int? = null
        var height: Int? = null
        var text: String = ""
        var alttext: String = ""
        var crops = mutableMapOf<String, String>()
        var authors: MutableList<AuthorItem> = mutableListOf()
        var selectorType: String = "image"
        var textElement: ElementItem? = null
        var disableAutomaticCrop: Boolean = false

        fun build(): ImageItem? {
            val requiredData = RequiredImageItemData.from(this)
            if (requiredData.uuid != null && requiredData.height != null && requiredData.width != null) {
                return ImageItem(
                    id = requiredData.uuid,
                    type = type.replace("x-im/", ""),
                    uri = uri,
                    width = requiredData.width,
                    height = requiredData.height,
                    text = text,
                    alttext = alttext,
                    crops = crops,
                    authors = authors,
                    selectorType = selectorType,
                    textElement = textElement,
                    disableAutomaticCrop = disableAutomaticCrop
                )
            }
            return null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is ImageItem) {
            if (other.urlBuilder != null && urlBuilder != null) {
                return urlBuilder?.getUri(100, 100) == other.urlBuilder?.getUri(100, 100) &&
                        text == other.text &&
                        authors == other.authors

            } else {
                return id == other.id
            }
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + text.hashCode()
        result = 31 * result + alttext.hashCode()
        result = 31 * result + crops.hashCode()
        result = 31 * result + authors.hashCode()
        result = 31 * result + (slideshowExtras?.hashCode() ?: 0)
        result = 31 * result + selectorType.hashCode()
        result = 31 * result + contentPartSource.hashCode()
        result = 31 * result + themeKey.hashCode()
        result = 31 * result + themeKeys.hashCode()
        result = 31 * result + (placeholderImage?.hashCode() ?: 0)
        result = 31 * result + (urlBuilder?.hashCode() ?: 0)
        result = 31 * result + cropDatas.hashCode()
        result = 31 * result + (defaultCrop?.hashCode() ?: 0)
        result = 31 * result + slideshowImageList.hashCode()
        result = 31 * result + matchingQuery.hashCode()
        result = 31 * result + textToShow.hashCode()
        result = 31 * result + typeIdentifier.hashCode()
        return result
    }

    companion object {
        private const val TEXTS_NONE = "none"
        private const val TEXTS_ALL = "all"
        private const val TEXTS_TEXT = "text"
        private const val TEXTS_AUTHOR = "author"
        val NO_CROP = CropData(0.0, 0.0, 1.0, 1.0)
        const val MAX_ALLOWED_SIZE = 1920.0

        fun builder(init: Builder.() -> Unit) = Builder(init)
    }
}

data class CropData(
    val x: Double = 0.0,
    val y: Double = 1.0,
    val width: Double = 0.0,
    val height: Double = 1.0,
) {

    companion object {
        fun fromUri(uri: String): CropData {
            val pathSegments = Uri.parse(uri).pathSegments
            return CropData(
                x = pathSegments[0].toDouble(),
                y = pathSegments[1].toDouble(),
                width = pathSegments[2].toDouble(),
                height = pathSegments[3].toDouble())
        }
    }

    operator fun times(other: CropData): CropData {
        return CropData(x * other.x, y * other.y, width * other.width, height * other.height)
    }

    operator fun times(imageItem: ImageItem): CropData {
        return CropData(
            x = x * imageItem.width,
            y = y * imageItem.height,
            width = width * imageItem.width,
            height = height * imageItem.height)
    }

    fun toRectF(): RectF {
        return RectF(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())
    }
}

data class RequiredImageItemData(val uuid: String?, val width: Int?, val height: Int?) {
    companion object {
        fun from(imageItemBuilder: ImageItem.Builder) =
            RequiredImageItemData(imageItemBuilder.uuid, imageItemBuilder.width, imageItemBuilder.height)
    }
}