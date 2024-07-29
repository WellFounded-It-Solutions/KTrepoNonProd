package se.infomaker.datastore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.Query
import io.reactivex.Maybe

@Dao
interface FrequencyDao {

    @Query("SELECT * FROM frequency")
    fun getAll(): Maybe<List<FrequencyRecord>>

    @Query("SELECT * FROM frequency WHERE frequency.uuid= :uuid")
    fun get(uuid: String): FrequencyRecord

    @Insert(onConflict = ABORT)
    fun insert(frequencyRecord: FrequencyRecord)

    @Query("DELETE FROM frequency WHERE frequency.uuid= :uuid")
    fun delete(uuid: String)
}