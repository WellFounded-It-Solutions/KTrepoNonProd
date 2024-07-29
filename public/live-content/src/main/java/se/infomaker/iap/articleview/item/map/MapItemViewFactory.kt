package se.infomaker.iap.articleview.item.map

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.view.LifecycleProxy
import se.infomaker.iap.map.Coordinates
import se.infomaker.iap.map.Interaction
import se.infomaker.iap.map.MapOptions
import se.infomaker.iap.map.MapViewHolderFactory
import se.infomaker.iap.theme.Theme
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapItemViewFactory @Inject constructor(
    private val mapViewHolderFactory: MapViewHolderFactory
) : ItemViewFactory {

    override fun typeIdentifier() = MapItem::class.java

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.empty_map_holder, parent, false)
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        // NOOP
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        if (item is MapItem) {
            val interactionType = Interaction.valueOf((item.interaction ?: MapItemPreprocessorConfig.INTERACTIVE).toUpperCase())

            val lifecycle = (view as? LifecycleProxy)?.getLifecycle()
            view.findViewById<FrameLayout>(R.id.map_holder)?.apply {
                val coordinates = item.latLngAsCoordinates()
                mapViewHolderFactory.create().also {
                    it.initMapWithOptions(context, mapOptions = MapOptions(
                        coordinates = coordinates,
                        zoomLevel = item.zoom.toFloat(),
                        interaction = interactionType
                    ))
                    lifecycle?.addObserver(it)
                    it.mapView?.let { mapView ->
                        addView(mapView)
                    }
                }
                updateLayoutParams {
                    height = item.resolveHeight(width)
                }
            }
        }
    }

    private fun MapItem.resolveHeight(defaultWidth: Int): Int {
        return try {
            heightForWidth(Resources.getSystem().displayMetrics.widthPixels, aspectRatio ?: DEFAULT_ASPECT_RATIO)
        }
        catch (e: IllegalArgumentException) {
            Timber.d("Using fallback aspect ratio 16:9 instead of $aspectRatio")
            heightForWidth(defaultWidth, DEFAULT_ASPECT_RATIO)
        }
    }

    private fun heightForWidth(width: Int, aspectRatio: String): Int {
        val parts = aspectRatio.split(":")
        val ratio: Double = when (parts.size) {
            1 -> { parts[0].toDouble() }
            2 -> {
                val widthPart  = parts[0].toDouble()
                val heightPart = parts[1].toDouble()
                widthPart / heightPart
            }
            else -> {
                throw IllegalArgumentException("AspectRatio is invalid, aspectRatio=$aspectRatio")
            }
        }
        return (width / ratio).toInt()
    }

    companion object {
        val LAYOUT_PARAMS_MATCH_WRAP = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        const val DEFAULT_ASPECT_RATIO = "16:9"
    }
}

private fun MapItem.latLngAsCoordinates(): Coordinates =
    Coordinates(lat.toDouble(), lng.toDouble())