package se.infomaker.livecontentui.bookmark.sync

import se.infomaker.datastore.Bookmark
import se.infomaker.livecontentmanager.parser.PropertyObject

data class BookmarkUpdate(val remoteUpdate: PropertyObject, val localSource: Bookmark?) {

    fun updatedBookmarkOrNull(): Bookmark? {
        if (localSource != null) {
            return Bookmark(
                remoteUpdate.id,
                remoteUpdate.properties,
                localSource.moduleId,
                false,
                bookmarkedDate = localSource.bookmarkedDate
            )
        }
        return null
    }
}