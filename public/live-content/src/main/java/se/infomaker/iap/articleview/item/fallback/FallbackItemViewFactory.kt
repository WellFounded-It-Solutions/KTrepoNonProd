package se.infomaker.iap.articleview.item.fallback

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.OneShotPreDrawListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.view.extractViews
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor
import timber.log.Timber

class FallbackItemViewFactory(val layout: String = "fallback_item", private val factoryTypeIdentifier: Any = FallbackItem::class.java) : ItemViewFactory {
    override fun typeIdentifier(): Any {
        return factoryTypeIdentifier
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        var identifier = resourceManager.getLayoutIdentifier(layout)
        if (identifier == 0 && layout != "fallback_item") {
            identifier = resourceManager.getLayoutIdentifier("fallback_item")
        }
        if (identifier == 0) {
            identifier = R.layout.default_fallback_item
        }

        return LayoutInflater.from(parent.context).inflate(identifier, parent, false).apply {
            (this as? ViewGroup)?.setTag(R.id.holder, FallbackViewHolder(this))
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        theme.apply(view)
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        view.setOnClickListener(null)
        if (item is FallbackItem && view is ViewGroup) {
            val fallbackViewHolder = FallbackViewHolder(view)
            val textViews = fallbackViewHolder.textViews

            textViews.forEach { (key, view) ->
                item.allAttributes[key]?.let {
                    view.text = it
                    view.visibility = View.VISIBLE
                } ?: let {
                    view.visibility = View.GONE
                }
            }

            fallbackViewHolder.image?.let { imageView ->
                imageView.setImageDrawable(null)
                if (allowChangingViewHeight(imageView)) {
                    val layoutParams = imageView.layoutParams
                    layoutParams.height = calculateHeight(imageView, item)
                    imageView.layoutParams = layoutParams
                }
                OneShotPreDrawListener.add(view) {
                    loadImage(item, fallbackViewHolder, imageView, view)
                }
            }

            if (isValidUrl(item.webUrl)) {
                view.setOnClickListener {
                    val statsBuilder = StatisticsEvent.Builder()
                    statsBuilder.event("openFallbackUrl")
                    statsBuilder.moduleId(moduleId)
                    statsBuilder.moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId))
                    statsBuilder.moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId))
                    statsBuilder.attribute("url", item.webUrl)
                    StatisticsManager.getInstance().logEvent(statsBuilder.build())

                    val builder = CustomTabsIntent.Builder()
                    val theme = ThemeManager.getInstance(view.context).getModuleTheme(moduleId)
                    val primary = theme.getColor("primaryColor", ThemeColor.TRANSPARENT)
                    val color = if (primary === ThemeColor.TRANSPARENT) Color.DKGRAY else primary.get()
                    builder.setToolbarColor(color)
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(view.context, Uri.parse(item.webUrl))
                }
            }
        }
    }

    private fun loadImage(item: FallbackItem, fallbackViewHolder: FallbackViewHolder, imageView: ImageView, view: ViewGroup) {
        if (item.imageUrl != null) {
            val layoutParams = imageView.layoutParams
            imageView.visibility = View.VISIBLE
            val imageHeight: Int
            if (allowChangingViewHeight(imageView)) {
                if (item.imageWidth != null && item.imageHeight != null) {
                    imageHeight = calculateHeight(imageView, item)
                    layoutParams.height = imageHeight
                    if (imageHeight != 0) {
                        fallbackViewHolder.errorView?.layoutParams?.height = imageHeight
                    }
                } else {
                    Timber.w("Fallback does not provide height/width")
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                imageView.layoutParams = layoutParams
            }

            Glide.with(view.context)
                    .load(item.imageUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?,
                                                  target: Target<Drawable>?,
                                                  isFirstResource: Boolean): Boolean {
                            toggleVisibility(fallbackViewHolder, true)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?,
                                                     target: Target<Drawable>?,
                                                     dataSource: DataSource?,
                                                     isFirstResource: Boolean): Boolean {
                            toggleVisibility(fallbackViewHolder, false)
                            return false
                        }
                    })
                    .into(imageView)
        } else {
            imageView.layoutParams.height = 0
            imageView.visibility = View.GONE
            fallbackViewHolder.errorView?.visibility = View.GONE
        }
    }

    private fun allowChangingViewHeight(imageView: ImageView): Boolean {
        (imageView.parent as? ConstraintLayout)?.let { parent ->
            val constraintSet = ConstraintSet()
            constraintSet.clone(parent)
            val constraint = constraintSet.getConstraint(imageView.id)

            // If someone is trying hard to set a specific ratio for this image, don't allow changes to view height.
            return constraint.layout.dimensionRatio == null
        }
        return true
    }

    private fun calculateHeight(imageView: ImageView, item: FallbackItem): Int {
        if (imageView.width == 0) {
            return 0
        }
        val scale: Double = imageView.width / (item.imageWidth ?: 1).toDouble()
        val height = item.imageHeight ?: 0
        return (scale * height).toInt()
    }

    private fun toggleVisibility(fallbackViewHolder: FallbackViewHolder, showErrorView: Boolean) {
        fallbackViewHolder.errorView?.visibility = if (showErrorView) View.VISIBLE else View.GONE
        fallbackViewHolder.image?.visibility = if (showErrorView) View.GONE else View.VISIBLE
    }

    private fun isValidUrl(url: String?): Boolean {
        url?.let {
            val p = Patterns.WEB_URL
            val m = p.matcher(it)
            return m.matches()
        }
        return false
    }
}

private class FallbackViewHolder(view: ViewGroup) {
    val textViews: Map<String, TextView> = view.extractViews().filter { it.value is TextView }.map { it.key to it.value as TextView }.toMap()
    val image: ImageView? by lazy { view.findViewById(R.id.image) as? ImageView }
    val errorView: LinearLayout? by lazy { view.findViewById(R.id.loadingError) as? LinearLayout }
}
