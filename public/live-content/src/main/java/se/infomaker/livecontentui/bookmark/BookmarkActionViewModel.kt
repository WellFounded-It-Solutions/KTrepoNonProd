package se.infomaker.livecontentui.bookmark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import se.infomaker.datastore.DatabaseSingleton

class BookmarkActionViewModel(uuid: String) : ViewModel() {

    private val _isBookmarked: LiveData<Boolean>

    val isBookmarked: LiveData<Boolean>
        get() = _isBookmarked

    init {
        val liveBookmark = DatabaseSingleton.getDatabaseInstance().bookmarkDao().get(uuid)
        _isBookmarked = MediatorLiveData<Boolean>().also {
            it.addSource(liveBookmark) { bookmark ->
                it.value = bookmark != null
            }
        }
    }
}