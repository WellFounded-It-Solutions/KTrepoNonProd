package se.infomaker.iap.articleview.item.image

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import se.infomaker.frtutilities.ResourceManager
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.image.ThemeImage
import se.infomaker.iap.theme.view.ThemeableTextView

class ImageGalleryItemViewFactory : ItemViewFactory {
    override fun typeIdentifier(): Any {
        return ImageGalleryItem::class.java
    }

    private val imageItemViewGallery = ImageItemViewFactory()

    companion object {
        val LAYOUT_PARAMS_MATCH_WRAP = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {

        return LinearLayout(parent.context).apply {
            layoutParams = LAYOUT_PARAMS_MATCH_WRAP
            orientation = LinearLayout.VERTICAL

            addView(FrameLayout(parent.context).apply {
                layoutParams = LAYOUT_PARAMS_MATCH_WRAP

                addView(imageItemViewGallery.createView(this, resourceManager, theme).apply {
                    tag = "image"
                    if (this is IconOverlayProvider) {
                        this.getIconOverlay().addView(ThemeableTextView(this.context).apply {
                            tag = "text"
                            val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            this.layoutParams = layoutParams
                            layoutParams.gravity = Gravity.BOTTOM or Gravity.RIGHT

                            val padding = UI.dp2px(8f).toInt()
                            setPadding(padding, padding, padding, padding)

                            val margin = UI.dp2px(10f).toInt()
                            layoutParams.setMargins(margin, margin, margin, margin)

                            if (theme.getColor("imageGalleryCountTextBackground", null) == null) {
                                setBackgroundColor(Color.argb(128, 0, 0, 0))
                            } else {
                                themeBackgroundColor = "imageGalleryCountTextBackground"
                            }

                            theme.getImage("imageGalleryIcon", ThemeImage(R.drawable.image_gallery_icon))?.let {
                                setDrawables(it.getImage(context), null)
                                compoundDrawablePadding = padding
                            }

                            themeKeys = listOf("imageGalleryCountText")
                            if (theme.getText(themeKeys, null) == null) {
                                themeKeys = null
                                setTextColor(Color.WHITE)
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                            }
                        })
                    }
                })
            })
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        val gallery = item as ImageGalleryItem
        view.findViewWithTag<LinearLayout>("image")?.let { layout ->
            gallery.images.firstOrNull()?.let {
                imageItemViewGallery.themeView(layout, it, theme)
            }
        }
        view.findViewWithTag<ThemeableTextView>("text")?.apply(theme)
        view.findViewWithTag<ThemeableTextView>("description")?.apply(theme)
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        val gallery = item as ImageGalleryItem
        view.findViewWithTag<LinearLayout>("image")?.let { layout ->
            gallery.images.firstOrNull()?.let { firstImage ->
                val imageItem = if (gallery.text.isNotBlank()) firstImage.copy(textElement = gallery.textElement, text = gallery.text) else firstImage
                imageItemViewGallery.bindView(imageItem, layout, moduleId)
            }
        }
        view.findViewWithTag<ThemeableTextView>("text")?.apply {
            text = context.resources.getQuantityString(R.plurals.image_gallery_count, gallery.images.size, gallery.images.size)
        }
    }
}