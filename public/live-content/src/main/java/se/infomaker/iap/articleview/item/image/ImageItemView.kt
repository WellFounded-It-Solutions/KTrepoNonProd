package se.infomaker.iap.articleview.item.image

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView

class ImageItemView(context: Context) : LinearLayout(context), IconOverlayProvider {
    val imageView: ThemeableImageView
    val imageCaption: ThemeableTextView?
    val imagePhotographer: ThemeableTextView?
    private var imageOverlay: FrameLayout? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.image_item, this)
        imageView = findViewById(R.id.image_view)
        imageCaption = findViewById(R.id.image_view_text)
        imagePhotographer = findViewById(R.id.image_photographer)
        imageOverlay = findViewById(R.id.image_overlay)
    }

    override fun getIconOverlay(): FrameLayout {
        return imageOverlay ?: FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            imageOverlay = this
            addView(this)
        }
    }
}

