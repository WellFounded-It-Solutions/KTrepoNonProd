package se.infomaker.livecontentmanager.config

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class StreamConfig(
    @SerializedName("contentProvider") private val _contentProvider: String? = null,
    @SerializedName("baseQuery") private val _baseQuery: JsonObject? = null
): Parcelable {
    val contentProvider: String
        get() = _contentProvider ?: ""
    val baseQuery: JsonObject
        get() = _baseQuery ?: JsonObject()

    data class BaseQueryConfig(
        @SerializedName("must") private val _must: List<TermConfig>? = null,
        @SerializedName("should") private val _should: List<TermConfig>? = null,
    ) {
        val must: List<TermConfig>
            get() = _must ?: emptyList()
        val should: List<TermConfig>
            get() = _should ?: emptyList()
        var minimum_should_match: Int = 0
    }

    data class TermConfig(
        @SerializedName("term") private val _term: Map<String, String>? = null
    ) {
        val term: Map<String, String>
            get() = _term ?: emptyMap()
    }

    private companion object : Parceler<StreamConfig> {
        override fun StreamConfig.write(parcel: Parcel, flags: Int) {
            parcel.writeStringArray(arrayOf(
                contentProvider,
                baseQuery.toString()
            ))
        }

        override fun create(parcel: Parcel): StreamConfig {
            val stringArray = arrayOfNulls<String>(2)
            parcel.readStringArray(stringArray)

            val contentProvider = stringArray[0]
            val baseQuery = stringArray[1]?.let {
                try {
                    JsonParser.parseString(it).asJsonObject
                }
                catch (e: JsonParseException) {
                    Timber.e("Failed to parse JsonObject from: $it")
                    null
                }
            }
            return StreamConfig(contentProvider, baseQuery)
        }
    }
}