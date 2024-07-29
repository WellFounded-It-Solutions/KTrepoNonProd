package se.infomaker.livecontentui.bookmark.sync

import com.navigaglobal.mobile.follow.migration.extensions.objects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import se.infomaker.datastore.Bookmark
import se.infomaker.datastore.DatabaseSingleton
import se.infomaker.livecontentmanager.extensions.similar
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentmanager.parser.PropertyObjectParser
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService
import se.infomaker.livecontentui.common.di.GlobalLiveContentUiConfig
import se.infomaker.livecontentui.common.di.GlobalPropertyObjectParser
import se.infomaker.livecontentui.config.LiveContentUIConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarksSyncManager(
    private val openContentService: OpenContentService,
    private val liveContentUiConfig: LiveContentUIConfig,
    private val propertyObjectParser: PropertyObjectParser,
    private val scope: CoroutineScope
) {
    private val bookmarkDao = DatabaseSingleton.getDatabaseInstance().bookmarkDao()

    @Inject
    constructor(
        openContentService: OpenContentService,
        @GlobalLiveContentUiConfig liveContentUiConfig: LiveContentUIConfig,
        @GlobalPropertyObjectParser propertyObjectParser: PropertyObjectParser
    ) : this(
        openContentService,
        liveContentUiConfig,
        propertyObjectParser,
        CoroutineScope(Dispatchers.IO + Job())
    )

    /**
     * TODO
     *
     * We are not currently storing the original typePropertyMap for each bookmark, meaning
     * the first sync per bookmark _MIGHT_ be an update since a global article typePropertyMap
     * is probably not what all articles are using originally. (i.e. We usually care about related
     * content in other modules).
     *
     * Discussed with the iOS team on 2022-08-09 and was ignored for now.
     */
    fun sync() = scope.launch {
        val bookmarks = bookmarkDao.getAll()
        val uuids = bookmarks.map { it.uuid }
        openContentService.objects(uuids, liveContentUiConfig.liveContent, propertyObjectParser).getOrNull()?.let { remoteContent ->
            deleteUnpublishedBookmarks(bookmarks, remoteContent)
            updateChangedBookmarks(bookmarks, remoteContent)
        }
    }

    private fun deleteUnpublishedBookmarks(bookmarks: List<Bookmark>, remoteContent: List<PropertyObject>) {
        val unpublishedBookmarks = bookmarks.filterNot { existing -> remoteContent.map { it.id }.contains(existing.uuid) }
        if (unpublishedBookmarks.isNotEmpty()) {
            Timber.d("Deleting unpublished bookmark(s): ${unpublishedBookmarks.joinToString { it.uuid }}")
            unpublishedBookmarks.forEach { bookmarkDao.delete(it) }
        }
    }

    private fun updateChangedBookmarks(bookmarks: List<Bookmark>, remoteContent: List<PropertyObject>) {
        val changedUuids = bookmarks.filter { existing ->
            remoteContent.firstOrNull { existing.uuid == it.id }?.let { remote ->
                !existing.properties.similar(remote.properties)
            } ?: false
        }.map { it.uuid }
        val changedBookmarks = remoteContent.filter { changedUuids.contains(it.id) }
            .map { remote -> BookmarkUpdate(remote, bookmarks.firstOrNull { it.uuid == remote.id }) }
            .mapNotNull { it.updatedBookmarkOrNull() }
        if (changedBookmarks.isNotEmpty()) {
            Timber.d("Updating changed bookmark(s): ${changedBookmarks.joinToString { it.uuid }}")
            changedBookmarks.forEach { bookmarkDao.insert(it) }
        }
    }
}