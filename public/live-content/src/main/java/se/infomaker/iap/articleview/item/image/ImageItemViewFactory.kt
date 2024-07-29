package se.infomaker.iap.articleview.item.image

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.OneShotPreDrawListener
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import se.infomaker.coremedia.slideshow.PlaceholderImage
import se.infomaker.coremedia.slideshow.SlideshowActivity
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.ktx.findActivity
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.ktx.suffixItems
import se.infomaker.iap.articleview.util.UI.dp2px
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.image.ThemeImage
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.livecontentui.livecontentrecyclerview.view.IMImageView
import timber.log.Timber


class ImageItemViewFactory : ItemViewFactory {

    companion object {
        val LAYOUT_PARAMS_MATCH_WRAP = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val DEFAULT_IMAGE_PLACEHOLDER = ThemeImage(R.drawable.default_placeholder_image)
        const val PADDING_HORIZONTAL = "PaddingHorizontal"
        const val PADDING_VERTICAL = "PaddingVertical"
        const val PADDING_TOP = "PaddingTop"
        const val PADDING_BOTTOM = "PaddingBottom"
    }

    override fun typeIdentifier(): Any = ImageItem::class.java

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View =
        ImageItemView(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(LAYOUT_PARAMS_MATCH_WRAP)
        }

    data class Padding(val left: Int, val top:Int, val right:Int, val bottom:Int)

    fun getViewPadding(themeKeys: List<String>, theme: Theme): Padding {
        val hotKey = themeKeys.suffixItems(PADDING_HORIZONTAL)
        val horizontalPadding = dp2px(theme.getSize(themeKeys.suffixItems(PADDING_HORIZONTAL), ThemeSize.ZERO).size).toInt()
        val verticalPaddingKeys = themeKeys.suffixItems(PADDING_VERTICAL)
        val topPaddingKeys = themeKeys.suffixItems(PADDING_TOP).zip(verticalPaddingKeys).flatMap { listOf(it.first, it.second) }
        val bottomPaddingKeys = themeKeys.suffixItems(PADDING_BOTTOM).zip(verticalPaddingKeys).flatMap { listOf(it.first, it.second) }
        val topPadding = dp2px(theme.getSize(topPaddingKeys, ThemeSize.ZERO).size).toInt()
        val bottomPadding = dp2px(theme.getSize(bottomPaddingKeys, ThemeSize.ZERO).size).toInt()
        return Padding(horizontalPadding, topPadding, horizontalPadding, bottomPadding)
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        if (item is ImageItem && view is LinearLayout) {
            item.placeholderImage = theme.getImage("placeholderImage", DEFAULT_IMAGE_PLACEHOLDER).getImage(view.context)

            val outerViewPadding = getViewPadding(item.themeKeys, theme)
            view.findViewById<ThemeableImageView>(R.id.image_view)?.apply {
                apply(theme)
                // 1. Apply outer view padding to left and right of the image, since we are slapping
                // default padding on the image texts
                updatePadding(left = outerViewPadding.left, right = outerViewPadding.right)
            }
            view.findViewById<ThemeableTextView>(R.id.image_view_text)?.apply {
                themeKeys = listOf(item.type + "Text", "default")
                apply(theme)
                getViewPadding(themeKeys, theme).apply { setPadding(left, top, right, bottom) }
            }
            view.findViewById<ThemeableTextView>(R.id.image_photographer)?.apply {
                themeKeys = listOf(item.type + "AuthorText", item.type + "Text", "default")
                getViewPadding(themeKeys, theme).apply { setPadding(left, top, right, bottom) }
                apply(theme)
            }
            // 2. Also apply outer view padding to top and bottom for backwards compatibility
            view.updatePadding(top = outerViewPadding.top, bottom = outerViewPadding.bottom)
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        if (item is ImageItem && view is LinearLayout) {
            view.findViewById<IMImageView>(R.id.image_view)?.let { imageView ->
                imageView.themeKey = item.themeKey
                imageView.setTag(R.id.parallax, true)

                if (item.alttext.isNotEmpty()) {
                    imageView.contentDescription = item.alttext
                }

                imageView.aspectRatio = item.aspectRatio
                imageView.requestLayout()

                val loadImage = {
                    val imageWidth = imageView.width - imageView.paddingLeft - imageView.paddingRight
                    val imageDimensionsForScaledWidth = item.imageDimensionsForScaledWidth(imageWidth.toDouble())
                    val imageUri = item.urlBuilder?.getUri(imageWidth, imageDimensionsForScaledWidth.height.toInt())

                    //TODO: This does not seem optimal; Also, we might want to set the placeholders of the images not yet bound
                    item.slideshowImageList.forEach { imageObject ->
                        if (item.id in imageObject.url) {
                            imageObject.placeholderImage = PlaceholderImage(imageUri.toString(), null, item.defaultCrop?.toRectF())
                        }
                        imageObject.cropUrl?.let {
                            if (item.id in imageObject.url) {
                                imageObject.placeholderImage = PlaceholderImage(imageUri.toString(), null, ImageItem.NO_CROP.toRectF())
                            }
                        }
                    }

                    if (imageUri != null) {
                        imageView.setOnClickListener { imageView ->
                            val activity = imageView.findActivity() ?: return@setOnClickListener
                            if (item.slideshowImageList.size > 0) {
                                var url = item.urlBuilder?.getFullUri().toString()
                                item.slideshowImageList.forEach {
                                    if (item.id in it.url) {
                                        url = if (it.cropUrl.isNullOrEmpty()) {
                                            imageUri.toString()
                                            return@forEach
                                        } else {
                                            it.cropUrl!!
                                        }
                                    }
                                }
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageView, url)
                                val slideshowIntent = SlideshowActivity.createIntent(
                                        context = activity,
                                        currentImage = item.id,
                                        images = item.slideshowImageList,
                                        extras = item.slideshowExtras)
                                activity.startActivity(slideshowIntent, options.toBundle())
                            } else {
                                Timber.d("SlideshowImageList is empty, cannot start slideshow.")
                            }
                        }
                    } else {
                        imageView.setOnClickListener(null)
                    }

                    val placeholderBuilder = Glide.with(imageView.context)
                            .load(item.preloadUri(imageView.context))
                            .onlyRetrieveFromCache(true)

                    item.placeholderImage?.let {
                        placeholderBuilder.error(it)
                    }

                    Glide.with(imageView.context)
                            .load(imageUri)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .thumbnail(Glide.with(imageView.context).load(imageView.findActivity()?.let {
                                item.previewUri()
                            }))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(placeholderBuilder)
                            .into(imageView)
                }

                OneShotPreDrawListener.add(imageView) {
                    loadImage.invoke()
                }
            }

            view.findViewById<ThemeableTextView>(R.id.image_view_text)?.apply {
                visibility = View.GONE
                val stringBuilder = item.descriptionSpannableStringBuilder
                if (!stringBuilder.isNullOrEmpty()) {
                    setText(stringBuilder, TextView.BufferType.SPANNABLE)
                    visibility = View.VISIBLE
                } else if (!item.getDescription().isNullOrEmpty()){
                    text = item.getDescription()
                    visibility = View.VISIBLE
                }
            }

            view.findViewById<ThemeableTextView>(R.id.image_photographer)?.apply {
                item.authorListAsStringOrNull?.let {
                    val prefix = context.resources.getString(R.string.photographer)
                    text = if (prefix.isNotEmpty()) "$prefix $it" else it
                    visibility = View.VISIBLE
                } ?: run {
                    visibility = View.GONE
                }
            }
        }
    }
}