package se.infomaker.googleanalytics.register


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(hit: Hit)

    @Delete
    fun delete(hit: Hit)

    @Query("SELECT * FROM hits LIMIT 20")
    fun nextTwenty(): List<Hit>

    @Query("SELECT COUNT(*) FROM hits")
    fun numberOfHits(): Int
}
