package se.infomaker.iap.articleview.extensions.ifragasatt.api

import android.net.Uri
import org.json.JSONObject
import se.infomaker.iap.articleview.extensions.ifragasatt.ImageUrlProviderConfig
import se.infomaker.iap.articleview.extensions.ifragasatt.firstStringOrNull
import se.infomaker.iap.articleview.item.image.CropData
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.item.image.ImageItemPreprocessor
import java.util.*

class IfragasattUrlBuilder(val config: ImageUrlProviderConfig, val properties: JSONObject) {

    var imageUrl:Uri? = null

    init {
        config.baseUrl?.let { baseUrl ->
            val imageId = config.uuidProperty?.let {
                properties.firstStringOrNull(it)
            }

            val imageUri = Uri.parse(baseUrl)

            val imageProviderProperty = config.imageProviderProperty?.let {
                properties.firstStringOrNull(it)
            } ?: "imengine"

            val builder = ImageItem.builder {
                type = "default"
                uuid = imageId ?: UUID.randomUUID().toString()
                uri = imageUri.toString()
            }

            builder.build()?.let { imageItem ->
                ImageItemPreprocessor().processPath(imageItem, baseUrl, imageProviderProperty)

                val crop = config.cropProperty?.let {
                    properties.firstStringOrNull(it)?.let {crop ->
                        val pathSegments = Uri.parse(crop).pathSegments
                        CropData(
                            x = pathSegments[0].toDouble(),
                            y = pathSegments[1].toDouble(),
                            width = pathSegments[2].toDouble(),
                            height = pathSegments[3].toDouble())


                    }
                } ?: CropData()
                imageUrl = imageItem.urlBuilder?.getUriForCrop(crop)
            }
        }
    }
}
