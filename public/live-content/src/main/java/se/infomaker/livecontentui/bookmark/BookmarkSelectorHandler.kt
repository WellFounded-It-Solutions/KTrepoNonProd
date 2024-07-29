package se.infomaker.livecontentui.bookmark

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.datastore.Bookmark
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.livecontentui.view.setBackgroundColor
import se.infomaker.livecontentui.view.setCloseButtonColor
import se.infomaker.livecontentui.view.setCloseButtonImageResource
import se.infomaker.streamviewer.extensions.getDrawableIdentifierOrFallback

class BookmarkSelectorHandler(
    private val moduleTheme: Theme,
    private val resourceManager: ResourceManager,
    private var listener: BookmarkSelectorListener,
    private val bookmarker: Bookmarker
) : ActionMode.Callback {

    private val titleTextColor: ThemeColor? by lazy { moduleTheme.getText("toolbarTitle", null)?.getColor(moduleTheme) }

    private val selected = mutableSetOf<Bookmark>()
    var selecting: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (!value) {
                    selected.clear()
                }
                listener.onModeChange(value, this)
            }
        }

    private var delete: MenuItem? = null
    private var actionMode : ActionMode? = null

    fun toggle(context: Context, bookmark: Bookmark): Boolean {
        return if (selected.contains(bookmark)) {
            deselect(context, bookmark)
            false
        }
        else {
            select(context, bookmark)
            true
        }
    }

    fun select(context: Context, bookmark: Bookmark) {
        if (!selecting) {
            selecting = true
        }
        selected.add(bookmark)
        setCount(context, selected.size)
    }

    fun deselect(context: Context, bookmark: Bookmark): Boolean {
        val removed = selected.remove(bookmark)
        setCount(context, selected.size)
        if (selected.size == 0) {
            selecting = false
        }
        return removed
    }

    private fun deleteSelected() {
        val toDelete = selected.toList()
        bookmarker.deleteAll(toDelete)
        listener.onDeleted(toDelete)
        selecting = false
    }

    fun isSelected(bookmark: Bookmark): Boolean {
        return selected.contains(bookmark)
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        if (item == delete) {
            deleteSelected()
            return true
        }
        return false
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = mode
        moduleTheme.getColor("toolbarColor", ThemeColor.WHITE).let {
            mode.setBackgroundColor(it.get())
        }
        delete = menu.add("")
        delete?.setIcon(resourceManager.getDrawableIdentifierOrFallback("action_delete", R.drawable.ic_delete_white_24dp))

        val closeButtonIdentifier = resourceManager.getDrawableIdentifier("action_close")
        if (closeButtonIdentifier > 0) {
            mode.setCloseButtonImageResource(closeButtonIdentifier)
        }

        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        moduleTheme.getColor("toolbarAction", ThemeColor.DKGRAY).let { color ->
            mode.setCloseButtonColor(color.get())
            delete?.icon?.setColorFilter(PorterDuffColorFilter(color.get(), PorterDuff.Mode.SRC_IN))
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        val set = selected.toList()
        actionMode = null
        selecting = false
        listener.onDismissed(set)
    }

    fun finish() {
        actionMode?.finish()
    }

    private fun setCount(context: Context, count: Int) {
        val titleText = context.resources.getQuantityString(R.plurals.bookmarks_selected, count, count)
        titleTextColor?.let { titleColor ->
            val spannableTitle = SpannableString(titleText)
            spannableTitle.setSpan(ForegroundColorSpan(titleColor.get()), 0, titleText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            actionMode?.title = spannableTitle
        } ?: run {
            actionMode?.title = titleText
        }
    }

    fun getSelectedIds(): ArrayList<String>? {
        return if (selecting) ArrayList(selected.map { it.uuid }) else null
    }
}