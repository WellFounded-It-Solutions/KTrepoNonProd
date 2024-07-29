package se.infomaker.livecontentui.livecontentrecyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.livecontentrecyclerview.binder.BinderCollection
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMFrameLayoutBinder
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMImageViewBinder
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMRecyclerViewBinder
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMTextViewBinder
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils
import se.infomaker.livecontentui.livecontentrecyclerview.view.OnClickObservable
import se.infomaker.livecontentui.livecontentrecyclerview.view.ViewClick


class PropertyObjectAdapter(
    resourceManager: ResourceManager,
    imageUrlFactory: ImageUrlBuilderFactory,
    private val itemLayout: Int,
    imageSizes: List<Double>,
    private val listKey: String?,
    propertyObject: PropertyObject?,
    private val theme: Theme,
    private val loopScroll: Boolean,
    private val childWidth: Int?,
    private val stretch: Boolean,
    private val context: JSONObject? = null
) : RecyclerView.Adapter<PropertyObjectViewHolder>(), OnClickObservable {

    private val propertyBinder: PropertyBinder = PropertyBinder(BinderCollection.with(IMImageViewBinder(imageUrlFactory, imageSizes), IMTextViewBinder(resourceManager), IMFrameLayoutBinder(), IMRecyclerViewBinder(resourceManager, imageUrlFactory, imageSizes, theme)))
    var items: List<PropertyObject>?
    private set
    private val viewClickSubject = PublishSubject.create<ViewClick>()
    var maxWidth: Int? = null

    init {
        items = extractItems(propertyObject)
    }

    private fun extractItems(propertyObject: PropertyObject?): List<PropertyObject>? {
        return if (!listKey.isNullOrEmpty()) {
            propertyObject?.optPropertyObjects(listKey)
        } else {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyObjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        return PropertyObjectViewHolder(view)
    }

    private fun shouldLoop(): Boolean {
        val size = items?.size ?: 0
        if (childWidth != null) {
            return loopScroll && maxWidth ?: 0 < size * childWidth
        }
        return loopScroll && size > 0
    }

    override fun getItemCount() : Int{
        // Do not try to render before we can size the views properly
        if (maxWidth == null) {
            return 0
        }
        return if (shouldLoop()) Integer.MAX_VALUE else items?.size ?: 0
    }

    private fun realPosition(position: Int) : Int {
        items?.let {
            return if (shouldLoop()) position % it.size else position
        }
        return position
    }

    override fun onBindViewHolder(holder: PropertyObjectViewHolder, position: Int) {
        items?.get(realPosition(position))?.let {
            propertyBinder.bind(it, DefaultUtils.getAllChildren(holder.itemView), context)
            theme.apply(holder.itemView)
            if (childWidth != null) {
                holder.itemView.layoutParams.width = childWidth()
            }

            holder.itemView.clicks()
                    .map { _ -> it.optString("contentId", null)?.let { contentId -> ViewClick("item", contentId) } }
                    .subscribe(viewClickSubject)
        }
    }

    private fun childWidth(): Int {
        maxWidth?.let { maxWidth ->
            if (childWidth != null) {
                val items = items?.size ?: 0
                if (maxWidth < items * childWidth || !stretch) {
                    return childWidth.toInt()
                }
                return childWidth + ((maxWidth - (items * childWidth))/items)
            }
        }
        return ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewClickSubject.onComplete()
    }

    fun update(propertyObject: PropertyObject?) {
        items = extractItems(propertyObject)
        notifyDataSetChanged()
    }

    override fun clicks(): Observable<ViewClick>? {
        return viewClickSubject
    }
}

class PropertyObjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)