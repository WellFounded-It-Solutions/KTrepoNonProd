package se.infomaker.datastore

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Observable

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmark ORDER BY bookmarkedDate DESC")
    fun all(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark ORDER BY bookmarkedDate DESC")
    suspend fun getAll(): List<Bookmark>

    @Query("SELECT * FROM bookmark WHERE NOT isDownloaded")
    fun needsDownload(): Observable<List<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE bookmark.uuid=:uuid")
    fun get(uuid: String): LiveData<Bookmark?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bookmark: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)
}