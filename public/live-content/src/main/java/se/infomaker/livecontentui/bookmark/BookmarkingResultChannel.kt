package se.infomaker.livecontentui.bookmark

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import se.infomaker.datastore.Bookmark

class BookmarkingResultChannel : ViewModel() {

    private val _result: PublishRelay<BookmarkingResult?> = PublishRelay.create()
    val result: Observable<BookmarkingResult?>
        get() = _result

    fun submit(bookmark: Bookmark, isBookmarked: Boolean) {
        _result.accept(BookmarkingResult(bookmark, isBookmarked))
    }
}