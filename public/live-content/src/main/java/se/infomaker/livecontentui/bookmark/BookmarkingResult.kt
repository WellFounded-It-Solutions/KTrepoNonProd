package se.infomaker.livecontentui.bookmark

import se.infomaker.datastore.Bookmark

data class BookmarkingResult(val bookmark: Bookmark, val isBookmarked: Boolean)