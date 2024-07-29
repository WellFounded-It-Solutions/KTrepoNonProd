package se.infomaker.livecontentui.extensions

import android.os.Bundle
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

internal fun Bundle.putJSONObject(key: String, jsonObject: JSONObject) {
    putString(key, jsonObject.toString())
}

internal fun Bundle.getJSONObjectOrNull(key: String): JSONObject? {
    try {
        getString(key)?.let {
            return JSONObject(it)
        }
    }
    catch (e: JSONException) {
        Timber.e("Could not convert $key in $this to JSONObject.")
    }
    return null
}