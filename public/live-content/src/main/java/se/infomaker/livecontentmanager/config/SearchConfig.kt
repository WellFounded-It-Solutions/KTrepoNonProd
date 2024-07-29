package se.infomaker.livecontentmanager.config


import com.google.gson.annotations.SerializedName
import se.infomaker.frtutilities.TextUtils
import se.infomaker.livecontentmanager.query.lca.SearchQuery
import java.io.Serializable

open class SearchConfig(
    @SerializedName("baseQuery") private val _baseQuery: String? = null,
    @SerializedName("contentType") private val _contentType: String? = null,
    @SerializedName("publicationDateKey") private val _publicationDateKey: String? = null,
): Serializable {
    var contentProvider: String? = null
    val baseQuery: String
        get() = _baseQuery ?: "Status:usable"
    val contentType: String
        get() = _contentType ?: "Article"
    @Deprecated("No longer supported in OpenContent 3.0, use sortIndex")
    var sortKey: String? = null
    var sortAscending: Boolean?= null
    var sortIndex: String? = null
    val publicationDateKey: String
        get() = _publicationDateKey ?: "Pubdate"
    var filters: String? = null

    fun getSort() : Map<String, String> {
        val index = if (TextUtils.isEmpty(sortIndex)) SearchQuery.DEFAULT_SORT_INDEX_FIELD else sortIndex as String
        val out = mutableMapOf<String, String>()
        out["sort.indexfield"] = index
        out["sort.${index}.ascending"] = if (sortAscending == true) "true" else "false"
        return out;
    }
}