package se.infomaker.iap.articleview.item.image

import android.content.Context
import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.OnPrepareView
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.Item

class ImageItemPreprocessor : Preprocessor {

    override fun process(
        content: ContentStructure,
        config: String,
        resourceProvider: ResourceProvider,
    ): ContentStructure {
        val imageConfig = Gson().fromJson(config, ImageItemPreprocessorConfig::class.java)
        if (imageConfig.baseUrl == null) {
            return content
        }

        imageConfig.baseUrl?.let { base ->
            content.body.items
                .filterIsInstance<ImageItem>()
                .filter { !it.uri.isNullOrEmpty() }
                .forEach { item ->
                    processImageSizeLimit(item, imageConfig.maxImageSizeLimit)
                    processCrops(item,
                        imageConfig.preferredCrops,
                        imageConfig.preferredCrop,
                        imageConfig.fallbackCrop)
                    processPath(
                        item,
                        base,
                        imageConfig.imageProvider,
                    )
                    preparePreloading(item)
                    processText(item, imageConfig.text)
                }
        }
        return content
    }

    fun processImageSizeLimit(item: ImageItem, maxSlideshowImageSize: Int?) {
        item.maxImageSizeLimit = maxSlideshowImageSize?.toDouble() ?: ImageItem.MAX_ALLOWED_SIZE
    }

    private fun determinePreferredCrop(
        cropDatas: MutableMap<String, CropData>,
        preferredCrops: List<String>?,
        preferredCrop: String?,
    ): CropData? {
        var finalCrop: CropData? = null
        if (cropDatas.isNullOrEmpty()) {
            return finalCrop
        }
        preferredCrops?.forEach { thePreferredCrop ->
            val matchingCropData = cropDatas.filter { it.key == thePreferredCrop }.map { it.value }
            if (matchingCropData.isNotEmpty()) {
                finalCrop = matchingCropData.first()
            }
            finalCrop?.let {
                return it
            }
        }
        preferredCrop?.let {
            finalCrop = cropDatas[it]
        }
        return finalCrop
    }

    fun processCrops(
        item: ImageItem,
        preferredCrops: List<String>?,
        preferredCrop: String?,
        fallbackCrop: String?,
    ): ImageItem {
        if (item.disableAutomaticCrop) {
            return item
        }

        item.crops.forEach { (cropRatio, crop) ->
            val pathSegments = Uri.parse(crop).pathSegments
            item.cropDatas[cropRatio] = CropData(
                x = pathSegments[0].toDouble(),
                y = pathSegments[1].toDouble(),
                width = pathSegments[2].toDouble(),
                height = pathSegments[3].toDouble())
        }
        item.defaultCrop = determinePreferredCrop(item.cropDatas, preferredCrops, preferredCrop)
        if (item.defaultCrop == null && fallbackCrop != null) {
            val cropDatas = fallbackCrop.split(":")

            if (cropDatas.size == 2) {
                val fallbackRatioWidth = cropDatas[0].toDouble()
                val fallbackRatioHeight = cropDatas[1].toDouble()
                val fallbackRatio = fallbackRatioWidth / fallbackRatioHeight
                val imageWidth = item.width
                val imageHeight = item.height
                val imageRatio = imageWidth.toDouble() / imageHeight

                //Crop fill width
                if (fallbackRatio > imageRatio) {
                    val fl = fallbackRatioHeight / fallbackRatioWidth
                    val scaledImageHeight = imageWidth * fl
                    val imageHeightRatio = scaledImageHeight / imageHeight

                    item.defaultCrop = CropData(
                        x = 0.0,
                        y = 0.0,
                        width = 1.0,
                        height = imageHeightRatio
                    )
                } else { //Crop fill height
                    val scaledImageWidth = imageHeight * fallbackRatio
                    val imageWidthRatio = scaledImageWidth / imageWidth

                    item.defaultCrop = CropData(
                        x = 0.5 - imageWidthRatio / 2,
                        y = 0.0,
                        width = imageWidthRatio,
                        height = 1.0
                    )
                }
            }
        }
        return item
    }

    fun processText(item: ImageItem, text: String): ImageItem {
        return item.apply {
            textToShow = text.split("|").distinct()
        }
    }

    fun processPath(
        item: ImageItem,
        base: String,
        imageProvider: String,
    ): ImageItem {
        when (imageProvider) {
            "imgix" -> {
                if (item.uri != null) {
                    item.urlBuilder = ImgixUrlBuilder(
                        base = Uri.parse(base),
                        imageItem = item,
                        defaultCropData = item.defaultCrop)
                }
            }
            else -> {
                if (item.uri != null) {
                    item.urlBuilder = ImEngineUrlBuilder(
                        base = Uri.parse(base),
                        imageItem = item,
                        defaultCropData = item.defaultCrop
                    )
                }
            }
        }
        return item
    }

    fun preparePreloading(item: ImageItem) {
        item.listeners.add(object : OnPrepareView {

            override fun onPreHeat(item: Item, context: Context) {
                (item as ImageItem).previewUri()?.let {
                    Glide.with(context).load(it).preload()
                }
            }

            override fun onPrepare(item: Item, view: View) {}

            override fun onPrepareCancel(item: Item, view: View) {}
        })
    }

    fun ContentStructure.optString(key: String, fallback: String?): String? {
        return properties.optJSONArray(key)?.optString(0, fallback)
    }
}