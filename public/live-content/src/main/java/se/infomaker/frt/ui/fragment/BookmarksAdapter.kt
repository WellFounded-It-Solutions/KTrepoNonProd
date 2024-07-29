package se.infomaker.frt.ui.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import se.infomaker.datastore.Bookmark
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ktx.getThemeableTouchColor
import se.infomaker.iap.theme.ktx.theme
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.livecontentui.bookmark.BookmarkSelectorHandler
import se.infomaker.livecontentui.bookmark.article.BookmarkPagerActivity
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils

class BookmarksAdapter(private val context: Context, private val moduleIdentifier: String, private val selectionHandlerFactory: () -> BookmarkSelectorHandler) : ListAdapter<Bookmark, BookmarkViewHolder>(Bookmark.DIFF_ITEM_CALLBACK) {

    private val resources by context.resources { moduleIdentifier }
    private val theme by context.theme { moduleIdentifier }
    private val itemViewBinder = PropertyBinder.getModuleDefault(context, moduleIdentifier)

    private var selectionHandler: BookmarkSelectorHandler? = null

    var forceAnimations = false
    var restoreState: ((List<Bookmark>) -> Unit)? = null

    fun saveInstanceState(outState: Bundle) {
        selectionHandler?.let {
            outState.putBoolean("isInSelectionMode", it.selecting)
            outState.putStringArrayList("selectedBookmarks", it.getSelectedIds())
        }
    }

    fun restoreInstanceState(inState: Bundle?) {
        if (inState != null && inState.getBoolean("isInSelectionMode", false)) {
            restoreState = { bookmarks ->
                restoreState = null
                selectionHandler = selectionHandlerFactory.invoke().also { selectionHandler ->
                    inState.getStringArrayList("selectedBookmarks")?.let { ids ->
                        bookmarks.filter { ids.contains(it.uuid) }.forEach { selectionHandler.select(context, it) }
                    }

                    notifyDataSetChanged()
                }
            }
            if (currentList.isNotEmpty()) {
                restoreState?.invoke(currentList)
            }
        }
    }

    override fun submitList(list: List<Bookmark>?) {
        super.submitList(list)
        if (restoreState != null && list?.isNotEmpty() == true) {
            restoreState?.invoke(list)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        var layoutResource = resources.getLayoutIdentifier("bookmark_teaser")
        if (layoutResource == 0) {
            layoutResource = R.layout.default_bookmark_teaser
        }
        val itemView = LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)
        return BookmarkViewHolder(itemView, theme)
    }

    override fun onBindViewHolder(viewHolder: BookmarkViewHolder, position: Int) {
        val bookmark = getItem(position)
        val propertyObject = bookmark.propertyObject

        theme.apply(viewHolder.itemView)
        viewHolder.setSelected(selectionHandler?.isSelected(bookmark) ?: false, forceAnimations)
        viewHolder.bookmark = bookmark
        setupOnClickListener(viewHolder, bookmark)
        setupOnLongClickListener(viewHolder, bookmark)
        itemViewBinder.bind(propertyObject, viewHolder.bindableViews, null)
    }

    private fun setupOnLongClickListener(viewHolder: BookmarkViewHolder, bookmark: Bookmark) {
        viewHolder.itemView.setOnLongClickListener {
            if (selectionHandler?.selecting != true) {
                selectionHandler = selectionHandlerFactory.invoke().also {
                    it.select(context, bookmark)
                }
                viewHolder.setSelected(selected = true, animated = true)
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener false
        }
    }

    private fun setupOnClickListener(viewHolder: BookmarkViewHolder, bookmark: Bookmark) {
        viewHolder.itemView.setOnClickListener {
            if (selectionHandler?.selecting == true) {
                val selected = selectionHandler?.toggle(context, bookmark) ?: false
                viewHolder.setSelected(selected, true)
            }
            else {
                openBookmark(viewHolder.itemView.context, bookmark)
            }
        }
    }

    fun cancelSelection() {
        selectionHandler?.selecting = false
    }

    private fun openBookmark(context: Context, bookmark: Bookmark) {
        (ModuleInformationManager.getInstance().getModuleInformation(moduleIdentifier)?.title ?: "").let { title ->
            BookmarkPagerActivity.openArticle(context, moduleIdentifier, title, bookmark.uuid)
        }
    }

    fun deselectItems(items: List<Bookmark>) {
        forceAnimations = true
        items.forEach {
            notifyItemChanged(currentList.indexOf(currentList.first { bookmark -> bookmark.uuid == it.uuid }))
        }
        forceAnimations = false
    }
}

class BookmarkViewHolder(itemView: View, private val theme: Theme) : RecyclerView.ViewHolder(itemView) {

    var bookmark: Bookmark? = null
    private val selectIndicator: ImageView? = itemView.findViewWithTag("select_indicator")
    private val selectIndicatorBackground: View? = itemView.findViewWithTag("select_indicator_background")

    init {
        selectIndicatorBackground?.visibility = View.INVISIBLE
        selectIndicator?.visibility = View.INVISIBLE
    }

    fun setSelected(selected: Boolean, animated: Boolean) {
        if (selectIndicator is ImageView && animated) {

            // Lets animate the checkbox baby
            if (selected) {
                selectIndicator.setImageResource(R.drawable.bookmark_check)
            }
            else {
                selectIndicator.setImageResource(R.drawable.bookmark_uncheck)
            }
            val drawable = selectIndicator.drawable
            selectIndicator.visibility = View.VISIBLE
            if (drawable is AnimatedVectorDrawable) {
                drawable.start()
            }
            else if (drawable is AnimatedVectorDrawableCompat) {
                drawable.start()
            }
        }
        else {
            selectIndicator?.setImageResource(R.drawable.bookmark_checkmark)
            selectIndicator?.visibility = if (selected) View.VISIBLE else View.INVISIBLE
        }
        if (animated) {
            if (selected) {
                selectIndicatorBackground?.fadeIn()
            }
            else {
                selectIndicatorBackground?.fadeOut()
            }
        }
        else {
            selectIndicatorBackground?.visibility = if (selected) View.VISIBLE else View.INVISIBLE
        }

        if (selected) {
            val touchColor = itemView.getThemeableTouchColor(theme).get()
            val backgroundTint = if (Color.alpha(touchColor) < 128) touchColor else ColorUtils.setAlphaComponent(touchColor, 128)
            itemView.backgroundTintList = ColorStateList.valueOf(backgroundTint)
        }
        else {
            itemView.backgroundTintList = null
        }
    }

    val bindableViews: List<View>? = DefaultUtils.getAllChildren(itemView)
}

fun View.fadeOut() {
    animate().alpha(0.0f)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setDuration(200)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.INVISIBLE
            }
        })
}

fun View.fadeIn() {
    alpha = 0f;
    visibility = View.VISIBLE
    animate().alpha(1.0f).setInterpolator(AccelerateDecelerateInterpolator())
        .setDuration(300)
        .setListener(null)
}