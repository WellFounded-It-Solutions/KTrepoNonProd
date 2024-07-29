package se.infomaker.iap.articleview.item.template.binder

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import androidx.core.view.OneShotPreDrawListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.image.CropData
import se.infomaker.iap.articleview.item.image.ImageItem

class ImageViewBinder : Binder {
    override fun bind(view: View, item: Item?) {
        if (item is ImageItem && view is ImageView) {
            view.setTag(R.id.parallax, true)

            val loadImage = {
                val crop = item.defaultCrop ?: CropData(0.0, 0.0, 1.0, 1.0)
                val croppedImageHeight = item.height * crop.height
                val croppedImageWidth = item.width * crop.width
                val scaledImageHeight = (croppedImageHeight * (view.width.toFloat() / croppedImageWidth)).toInt()
                val imageUri = item.urlBuilder?.getUri(view.width, scaledImageHeight)

                Glide.with(view.context)
                        .asBitmap()
                        .load(imageUri)
                        .transition(BitmapTransitionOptions.withCrossFade())
                        .apply(RequestOptions().placeholder(ColorDrawable(Color.BLACK)).fitCenter())
                        .into(view)
            }
            if (view.measuredWidth <= 0) {
                OneShotPreDrawListener.add(view) {
                    loadImage.invoke()
                }
            } else {
                loadImage.invoke()
            }
        } else {
            view.visibility = View.GONE
        }
    }
}