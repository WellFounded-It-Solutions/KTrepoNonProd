package se.infomaker.livecontentui.bookmark

import se.infomaker.datastore.Bookmark

interface BookmarkSelectorListener {
    fun onDismissed(selected: List<Bookmark>)
    fun onDeleted(deleted: List<Bookmark>)
    fun onModeChange(isSelecting: Boolean, selector: BookmarkSelectorHandler)
}