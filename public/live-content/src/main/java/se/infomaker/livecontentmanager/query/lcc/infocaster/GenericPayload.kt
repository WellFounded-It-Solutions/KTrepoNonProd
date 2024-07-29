package se.infomaker.livecontentmanager.query.lcc.infocaster

import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

data class GenericPayload(val value: String) {

    fun asJSONObjectOrNull() : JSONObject? {
        return try {
            JSONObject(value)
        }
        catch(e: JSONException) {
            Timber.w(e, "Unparsable payload")
            null
        }
    }

    fun <T> getPayload(classOfT: Class<T>) = GSON.fromJson(value, classOfT)

    companion object {
        private val GSON = Gson()
    }
}