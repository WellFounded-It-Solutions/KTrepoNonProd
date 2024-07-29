package se.infomaker.livecontentui.livecontentrecyclerview.binder

import org.json.JSONArray
import se.infomaker.iap.articleview.item.image.CropData
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.livecontentrecyclerview.view.IMImageView
import timber.log.Timber

class CropDataHelper {

    companion object {
        const val DEFAULT_CROP_AS_STRING = "16:9"
    }

    private var preferredCrops: List<CropDataHelperObject>? = null
    private var defaultCrop: CropDataHelperObject? = null
    var preferredCrop: String? = null
    var srcImageHeight: Double = 0.0
    var srcImageWidth: Double = 0.0
    var aspectRatioAsString: String = DEFAULT_CROP_AS_STRING

    fun parseCropData(imageView: IMImageView, properties: PropertyObject) {
        if (imageView.heightKeyPath.isNullOrEmpty() || imageView.widthKeyPath.isNullOrEmpty()) {
            return
        }

        try {
            srcImageHeight =
                imageView.heightKeyPath.let { properties.optString(it) }?.toDouble() ?: 1.0
            srcImageWidth =
                imageView.widthKeyPath.let { properties.optString(it) }?.toDouble() ?: 1.0
        } catch (e: NumberFormatException) {
            Timber.e(e, "Failed to resolve image size")
            return
        }

        imageView.cropKeyPath
            ?.let { properties.optString(it) }
            ?.let { CropData.fromUri(it) }
            ?.also { cropData ->
                defaultCrop = CropDataHelperObject(
                    "default",
                    cropData
                ).also {
                    it.ratio = it.ratioAsDouble(srcImageWidth, srcImageHeight)
                }
            }

        preferredCrop = imageView.preferredCrop
        preferredCrops = imageView.cropsKeyPath
            ?.let { properties.optJSONArray(it) }
            ?.let { parsePreferredCrops(it) }
    }

    private fun parsePreferredCrops(preferredCrops: JSONArray): List<CropDataHelperObject> {
        val cropDataHelperObjects = mutableListOf<CropDataHelperObject>()
        for (i in 0 until preferredCrops.length()) {
            val (cropId: String, cropData: String) = preferredCrops.getString(i).split("=")
            if (cropId.isNotBlank() && cropData.isNotBlank()) {
                cropDataHelperObjects.add(
                    CropDataHelperObject(
                        cropId,
                        CropData.fromUri(cropData)
                    ).also {
                        it.ratio = it.ratioAsDouble(srcImageWidth, srcImageHeight)
                    }
                )
            }
        }
        return cropDataHelperObjects
    }

    private fun buildCropDataFromPreferredCropKeyPath(): CropDataHelperObject? {
        preferredCrop?.let { crop ->
            if (!crop.contains(":")) {
                Timber.d("Invalid preferredCrop format, must contain ':'")
                return null
            }

            // Calculate the aspect ratios
            val cropRatio = crop.aspectRatio()
            val test = crop.split(":")
                .map { it.toDouble() }
                .reduce { acc, d -> acc / d }
            val imgRatio = srcImageWidth / srcImageHeight

            // Calculate the dimensions
            val w = (if (cropRatio > imgRatio) 1 else cropRatio / imgRatio).toDouble()
            val h = (if (cropRatio > imgRatio) imgRatio / cropRatio else 1).toDouble()

            // Center the crop
            val x = 0.5 - (w / 2)
            val y = 0.5 - (h / 2)

            return CropDataHelperObject(
                key = crop,
                cropData = CropData(x = x, y = y, width = w, height = h),
                ratio = cropRatio
            )
        }
        return null
    }

    fun findCrop(): CropData? {
        val alternateCrop = preferredCrop?.let { preferred ->
            preferredCrops?.firstOrNull() { preferred == it.key }
                ?: findAlternateCrop()
                ?: buildCropDataFromPreferredCropKeyPath()
        }
        return alternateCrop?.let {
            aspectRatioAsString = it.key
            it.cropData
        } ?: run {
            aspectRatioAsString = DEFAULT_CROP_AS_STRING
            defaultCrop?.cropData
        }
    }

    private fun findAlternateCrop(): CropDataHelperObject? {
        val crops = mutableListOf<CropDataHelperObject>().apply {
            defaultCrop?.let { add(it) }
            preferredCrops?.let { addAll(it) }
        }

        val targetAspectRatio = preferredCrop?.aspectRatio() ?: srcImageWidth / srcImageHeight

        var closestDistance = Double.MAX_VALUE
        var alternativeCropData: CropDataHelperObject? = null
        crops.forEach {
            it.ratio?.let { ratio ->
                val distance = ratio.distance(targetAspectRatio)
                if (distance < closestDistance) {
                    closestDistance = distance
                    alternativeCropData = it
                }
            }
        }
        return alternativeCropData ?: defaultCrop
    }
}

private fun Double.distance(to: Double): Double = if (this > to) this - to else to - this

data class CropDataHelperObject(val key: String, val cropData: CropData, var ratio: Double? = null)

fun CropDataHelperObject.ratioAsDouble(imageWidth: Double, imageHeight: Double): Double =
    imageWidth * this.cropData.width / imageHeight * this.cropData.height

fun String.aspectRatio(): Double {
    return this.split(":")
        .take(2)
        .map { it.toDouble() }
        .reduce { acc, d -> acc / d }
}