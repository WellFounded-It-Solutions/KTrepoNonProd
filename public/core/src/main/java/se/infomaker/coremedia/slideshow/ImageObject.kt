package se.infomaker.coremedia.slideshow

import android.graphics.Point
import android.graphics.RectF
import android.os.ParcelUuid
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ImageObject(
        val url: String,
        val cropUrl: String?,
        val description: String?,
        val photographers: List<String>?,
        val size: Point? = null,
        var placeholderImage: PlaceholderImage? = null,
        val uuid: String? = null) : Parcelable

@Parcelize
data class PlaceholderImage(var url: String = "",
                            var cropUrl: String?,
                            var crop: RectF? = null) : Parcelable