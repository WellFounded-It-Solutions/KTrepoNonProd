package se.infomaker.googleanalytics.register

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "hits")
data class Hit(@ColumnInfo(name = "query_params")
               val queryParams: String,
               @PrimaryKey
               @ColumnInfo(name = "hitId")
               val id: String = UUID.randomUUID().toString(),
               @ColumnInfo(name = "query_time")
               val queryTime: Long = System.currentTimeMillis())