package se.infomaker.livecontentui.config

import com.google.gson.annotations.SerializedName

data class MediaConfig(
    @SerializedName("image") private val _image: ImageConfig? = null,
    @SerializedName("captionFont") private val _captionFont: String? = null,
    @SerializedName("captionFontSize") private val _captionFontSize: Double? = null,
    @SerializedName("photographerTitle") private val _photographerTitle: String? = null,
    @SerializedName("video") private val _video: VideoConfig? = null
) {
    val image: ImageConfig
        get() = _image ?: ImageConfig()

    val captionFont: String
        get() = _captionFont ?: ""

    val captionFontSize: Double
        get() = _captionFontSize ?: 12.0

    val photographerTitle: String
        get() = _photographerTitle ?: ""

    val video: VideoConfig
        get() = _video ?: VideoConfig()

    data class VideoConfig(
        @SerializedName("aspectRatio") private val _aspectRatio: String? = null,
        @SerializedName("thumbnailLowresWidth") private val _thumbnailLowresWidth: Int? = null,
    ) {
        val aspectRatio: String?
            get() = _aspectRatio ?: "16:9"

        val thumbnailLowresWidth: Int
            get() = _thumbnailLowresWidth ?: 100
    }

    data class ImageConfig(
        @SerializedName("sizes") private val _sizes: List<Double>? = null,
        @SerializedName("aspectRatio") private val _aspectRatio: String? = null,
    ) {
        val sizes: List<Double>
            get() = _sizes ?: listOf(120.0, 320.0, 640.0, 800.0, 1024.0)

        val aspectRatio: String
            get() = _aspectRatio ?: "3:2"
    }
}

/*
"imageBaseUrl": "http://imengine.hall.infomaker.io",
     "image": {
         "sizes": [
           120,
           320,
           640,
           800,
           1024
         ],
         "aspectRatio": "3:2"
    },
    "captionFont": "Graphik-Regular-App.ttf",
    "captionFontSize": 15,
    "photographerTitle": "Foto",
    "video": {
      "aspectRatio": "16:9",
      "thumbnailLowresWidth": 100
    }
 */