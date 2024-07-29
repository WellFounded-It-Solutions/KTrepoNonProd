package se.infomaker.googleanalytics.register

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Hit::class],
    version = 1,
    exportSchema = false
)
abstract class GaDatabase : RoomDatabase() {
    abstract fun hitDao() : HitDao
}