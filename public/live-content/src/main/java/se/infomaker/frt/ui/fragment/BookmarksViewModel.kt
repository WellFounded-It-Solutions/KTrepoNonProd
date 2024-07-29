package se.infomaker.frt.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import se.infomaker.datastore.Bookmark
import se.infomaker.datastore.DatabaseSingleton

class BookmarksViewModel : ViewModel() {

    val data: LiveData<List<Bookmark>>
        get() = DatabaseSingleton.getDatabaseInstance().bookmarkDao().all()

}