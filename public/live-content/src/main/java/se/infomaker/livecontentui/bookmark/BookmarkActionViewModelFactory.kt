package se.infomaker.livecontentui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BookmarkActionViewModelFactory(private val uuid: String) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BookmarkActionViewModel(uuid) as T
    }
}