package se.infomaker.livecontentui.livecontentrecyclerview.binder

import android.view.View
import androidx.core.view.OneShotPreDrawListener
import org.json.JSONException
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.PropertyObjectAdapter
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory
import se.infomaker.livecontentui.livecontentrecyclerview.view.IMRecyclerView
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import timber.log.Timber

class IMRecyclerViewBinder(private val resourceManager: ResourceManager, private val imageUrlFactory: ImageUrlBuilderFactory, private val imageSizes: List<Double>, private val theme: Theme) : ViewBinder {

    private val supportedViews = setOf(IMRecyclerView::class.java)

    override fun supportedViews() = supportedViews

    override fun bind(view: View, value: String?, properties: PropertyObject): LiveBinding? {
        if (view !is IMRecyclerView) {
            throw RuntimeException("Unsupported view: $view")
        }

        val propertyObject = PropertyObject.fromString(value)

        val immutableAdapter = view.adapter
        if (immutableAdapter is PropertyObjectAdapter) {
            immutableAdapter.update(propertyObject)
            setupLoopScroll(view, immutableAdapter)
        }
        else {
            PropertyObjectAdapter(resourceManager, imageUrlFactory, view.itemLayout, imageSizes, getItemsKey(view), propertyObject, theme, view.loopScroll, view.childWidth?.toInt(), view.stretchToFill).let { adapter ->
                view.adapter = adapter
                OneShotPreDrawListener.add(view) {
                    val padding = view.paddingLeft + view.paddingRight
                    adapter.maxWidth = view.measuredWidth - padding
                    adapter.notifyDataSetChanged()
                    setupLoopScroll(view, adapter)
                }
            }
        }
        return null
    }

    private fun setupLoopScroll(view: IMRecyclerView, adapter: PropertyObjectAdapter) {
        if (view.loopScroll) {
            val startIndex = adapter.items?.let {
                if (it.isNotEmpty()) {
                    (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % it.size)
                } else {
                    0
                }
            } ?: 0
            view.layoutManager?.scrollToPosition(startIndex)
        }
    }

    private fun getItemsKey(view: IMRecyclerView): String? {
        view.itemsBindKeyPath?.let {
            return it
        }
        return null
    }

    override fun getKey(view: View): String? {
        if (view is IMRecyclerView) {
            view.bindKeyPath?.let {
                return it
            }
        }
        return if (view.id != 0) {
            view.resources.getResourceEntryName(view.id)
        }
        else null
    }
}

private fun PropertyObject.Companion.fromString(raw: String?): PropertyObject? {
    raw?.let {
        try {
            val jsonObject = JSONObject(it)
            return PropertyObject(jsonObject, jsonObject.getString("contentId"))
        }
        catch (e: JSONException) {
            Timber.e(e, "Failed to create property object from string: $raw")
        }
    }
    return null
}