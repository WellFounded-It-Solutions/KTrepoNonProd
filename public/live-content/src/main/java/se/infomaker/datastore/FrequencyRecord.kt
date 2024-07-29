package se.infomaker.datastore

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "frequency")
class FrequencyRecord (val uuid:String, val permission:String, val property:String?) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var created: Long = 0
        get() {
            if (field == 0L) {
                field = Date().time
            }
            return field
        }
}
