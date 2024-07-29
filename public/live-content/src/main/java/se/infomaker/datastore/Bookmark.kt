package se.infomaker.datastore

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONObject
import se.infomaker.frtutilities.DateUtil
import se.infomaker.livecontentmanager.extensions.similar
import se.infomaker.livecontentmanager.parser.PropertyObject
import java.util.Date

@Entity
data class Bookmark @JvmOverloads constructor(
    @PrimaryKey val uuid: String,
    val properties: JSONObject,
    val moduleId: String,
    val isDownloaded: Boolean,
    val pubDate: Long = getPublicationDate(properties)?.time ?: System.currentTimeMillis(),
    val bookmarkedDate: Long? = System.currentTimeMillis()
) {

    @delegate:Ignore
    val propertyObject by lazy { PropertyObject(properties, uuid) }

    companion object {

        @JvmStatic
        fun getPublicationDate(properties: JSONObject): Date? {
            val jsonArray = properties.optJSONArray("publicationDate")
            if (jsonArray != null && jsonArray.length() > 0) {
                val dateString = jsonArray.optString(0)
                return DateUtil.getDateFromString(dateString)
            }
            return null
        }

        val DIFF_ITEM_CALLBACK = object : DiffUtil.ItemCallback<Bookmark>() {

            override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark) = oldItem.uuid == newItem.uuid

            override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
                return oldItem.properties.similar(newItem.properties)
            }
        }
    }
}