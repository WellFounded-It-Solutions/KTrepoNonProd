package se.infomaker.livecontentui.bookmark


import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.jetbrains.annotations.NotNull
import org.json.JSONObject
import se.infomaker.datastore.Bookmark
import se.infomaker.datastore.DatabaseSingleton
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.connectivity.Connectivity
import se.infomaker.iap.articleview.ArticleConfig
import se.infomaker.iap.articleview.offline.HeadlessArticleLoader
import timber.log.Timber


/**
 * Downloads bookmarks that gets added
 */
class BookmarkDownloader(val context: @NotNull Context) {
    private val scope = CoroutineScope(newSingleThreadContext("BookmarkDownloader"))
    var disposable: Disposable? = null
    var shouldAutoDownload = false
    set(shouldDownload) {
        if (field == shouldDownload) {
            return
        }
        field = shouldDownload
        when(shouldDownload) {
            true -> {
                start()
            }
            false -> {
                stop()
            }
        }

    }
    private fun start() {
        if (disposable != null) {
            return
        }
        disposable = Observable.combineLatest(DatabaseSingleton.getDatabaseInstance().bookmarkDao().needsDownload(), Connectivity.observable(), { list, isConnected -> if (isConnected) list else emptyList() }).subscribe({
            if (it.isNotEmpty()) {
                download(it)
            }
        }, {
            Timber.e(it)
        })

    }

    private fun download(bookmarks: List<Bookmark>) {
        scope.launch {
            bookmarks.forEach { bookmark ->
                download(bookmark)
            }
        }
    }

    private suspend fun download(bookmark: Bookmark) {
        val config = ConfigManager.getInstance(context).getConfig(bookmark.moduleId, ArticleConfig::class.java)
        Timber.d("Downloading $bookmark")
        HeadlessArticleLoader(config).load(context, bookmark.moduleId, JSONObject(), bookmark.properties)
        DatabaseSingleton.getDatabaseInstance().bookmarkDao().insert(bookmark.copy(isDownloaded = true))

    }

    private fun stop(){
        disposable?.dispose()
        disposable = null

    }
}