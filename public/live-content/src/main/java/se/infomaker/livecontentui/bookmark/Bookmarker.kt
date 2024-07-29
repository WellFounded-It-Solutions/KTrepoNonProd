package se.infomaker.livecontentui.bookmark

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import se.infomaker.datastore.Bookmark
import se.infomaker.datastore.DatabaseSingleton
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.livecontentmanager.parser.PropertyObject
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.GlobalValueManager
import se.infomaker.frtutilities.ModuleInformation
import se.infomaker.frtutilities.ktx.config
import se.infomaker.iap.action.ActionManager
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Operation.Companion.create
import se.infomaker.livecontentui.AccessManager
import se.infomaker.livecontentui.StatsHelper
import se.infomaker.livecontentui.bookmark.config.BookmarkConfig
import timber.log.Timber

class Bookmarker(private val context: Context, private val moduleId: String) {

    constructor(view: View, _moduleId: String?) : this(view.context, _moduleId ?: "shared") {
        this.view = view
    }

    private var view: View? = null
    private val config by config<BookmarkConfig> { ModuleInformation(moduleId) }
    private val resources by context.resources { moduleId }

    fun showSnackbar(bookmark: Bookmark, bookmarked: Boolean) {
        view?.let {
            val text = if (bookmarked) resources.getString("bookmark_action_reverse_save", "Bookmark added") else resources.getString("bookmark_action_reverse_remove", "Bookmark removed")
            val snackbar = Snackbar.make(it, text, Snackbar.LENGTH_LONG)
            val actionText = resources.getString("bookmark_feedback_action", context.getString(R.string.bookmark_undo_action))
            snackbar.setAction(actionText) {
                resolveAction().invoke(bookmarked, bookmark)
            }
            snackbar.show()
        } ?: Timber.e("View cannot be null.")
    }

    private fun resolveAction(): (Boolean, Bookmark) -> Unit {
        config.bookmarkFeedbackAction?.let { action ->
            val operation = action.create(moduleId, GlobalValueManager.getGlobalValueManager(context))
            return { _, _ ->
                operation.perform(context) {}
            }
        }
        return { bookmarked, bookmark ->
            if (bookmarked) {
                delete(bookmark)
            } else {
                insert(bookmark)
            }
        }
    }

    fun insert(bookmark: Bookmark) {
        GlobalScope.launch {
            DatabaseSingleton.getDatabaseInstance().bookmarkDao().insert(bookmark)
            logBookmarkEvent(context, BookmarkEventType.BOOKMARKED, bookmark.propertyObject, moduleId)
        }
    }

    fun delete(bookmark: Bookmark) {
        GlobalScope.launch {
            DatabaseSingleton.getDatabaseInstance().bookmarkDao().delete(bookmark)
            logBookmarkEvent(context, BookmarkEventType.UNBOOKMARKED, bookmark.propertyObject, moduleId)
        }
    }

    fun insertAll(bookmarks: List<Bookmark>) {
        GlobalScope.launch {
            bookmarks.forEach {
                DatabaseSingleton.getDatabaseInstance().bookmarkDao().insert(it)
                logBookmarkEvent(context, BookmarkEventType.BOOKMARKED, it.propertyObject, moduleId)
            }
        }
    }

    fun deleteAll(bookmarks: List<Bookmark>) {
        GlobalScope.launch {
            bookmarks.forEach {
                DatabaseSingleton.getDatabaseInstance().bookmarkDao().delete(it)
                logBookmarkEvent(context, BookmarkEventType.UNBOOKMARKED, it.propertyObject, moduleId)
            }
        }
    }

    private fun logBookmarkEvent(context: Context, bookmarkEventType: BookmarkEventType, propertyObject: PropertyObject, moduleId: String) {
        StatsHelper.logBookmarkEvent(propertyObject, moduleId, bookmarkEventType.toString,
                AccessManager(context, moduleId).observeAccessAttributes(
                        Observable.just(propertyObject)).firstOrError())
    }
}

internal enum class BookmarkEventType(var toString: String) {
    BOOKMARKED("bookmarked"),
    UNBOOKMARKED("unbookmarked"),
}