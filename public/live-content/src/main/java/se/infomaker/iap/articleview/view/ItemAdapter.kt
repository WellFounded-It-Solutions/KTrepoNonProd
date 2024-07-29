package se.infomaker.iap.articleview.view

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory2
import se.infomaker.iap.articleview.item.ItemViewFactoryProvider
import se.infomaker.iap.articleview.item.author.clearDividers
import se.infomaker.iap.articleview.item.decorator.setMarginDecorators
import se.infomaker.iap.articleview.theme.UpdatableTheme
import se.infomaker.iap.theme.OnThemeUpdateListener
import se.infomaker.iap.theme.Theme
import timber.log.Timber
import java.util.Collections

class ItemAdapter @JvmOverloads constructor(
    content: ContentStructure,
    val moduleId: String,
    val resourceManager: ResourceManager,
    var theme: Theme,
    var viewFactoryProvider: ItemViewFactoryProvider,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val onItemFailed: (Item) -> Unit
) : ListAdapter<Item, ItemViewHolder>(ItemDiffCallback()), StickyHeaders, OnThemeUpdateListener, LifecycleObserver {

    private var itemTypes = mutableListOf<Any>()

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
        update(content, viewFactoryProvider)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        (theme as? UpdatableTheme)?.addOnUpdateListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        (theme as? UpdatableTheme)?.removeOnUpdateListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ItemViewHolder {
        val view = viewFactoryProvider.viewFactoryForType(itemTypes[type]).createView(parent, resourceManager, theme)
        (view as? LifecycleProxy)?.setLifecycle(lifecycleOwner?.lifecycle)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        item.listeners.forEach { it.onPrepare(item, viewHolder.itemView) }

        viewHolder.item = item
        val viewFactory = viewFactoryProvider.viewFactoryForItem(item)

        (viewFactory as? ItemViewFactory2)?.let {
            viewFactory.bindView(item, viewHolder.itemView, moduleId, viewHolder.layoutPosition) { item ->
                onItemFailed.invoke(item)
            }
            viewFactory.themeView(viewHolder.itemView, item, theme, viewHolder.layoutPosition)
        } ?: run {
            viewFactory.bindView(item, viewHolder.itemView, moduleId)
            viewFactory.themeView(viewHolder.itemView, item, theme)
        }
        item.decorators.forEach { it.decorate(item, viewHolder.itemView, theme) }
    }

    override fun onViewRecycled(holder: ItemViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.clearDividers()
        holder.itemView.setMarginDecorators(emptySet())
        holder.item?.let {
            it.listeners.forEach { listener ->
                if (holder.item != null) {
                    listener.onPrepareCancel(holder.item!!, holder.itemView)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val type = getItem(position).typeIdentifier
        val index = itemTypes.indexOf(type)
        if (index == -1) {
            Timber.e("$type has no match in $itemTypes")
        }
        return index
    }

    fun update(updated: ContentStructure, viewFactoryProvider: ItemViewFactoryProvider) {
        this.viewFactoryProvider = viewFactoryProvider

        itemTypes.addAll(updated.body.items.map { it.typeIdentifier }.distinct().filter {
            !itemTypes.contains(it)
        })

        /*
         * Pass an immutable copy of the items contained in the ContentStructure.
         * ContentStructure is unfortunately extremely mutable, so this is a safe guard for us to
         * at least make sure that the ItemAdapter is only handling immutable data.
         */
        submitList(updated.body.items.toImmutableList())
    }

    override fun isStickyHeader(position: Int): Boolean {
        return getItem(position).sticky
    }

    override fun onThemeUpdated() = notifyDataSetChanged()
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var item: Item? = null
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        val sameItem = oldItem.uuid == newItem.uuid
        if (!sameItem) {
            Timber.d("Items are not the same. Old [${oldItem.uuid}] vs. new [${newItem.uuid}]")
        }
        return sameItem
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        val sameContent = oldItem == newItem
        if (!sameContent) {
            Timber.d("Item contents are not the same. Old [${oldItem}] vs. new [${newItem}]")
        }
        return sameContent
    }
}

private fun <T> List<T>.toImmutableList(): List<T> {
    return Collections.unmodifiableList(toMutableList())
}
