package se.infomaker.iap.articleview.util

import android.os.Bundle
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

internal fun Bundle.getJSONObject(key: String): JSONObject? {
    try {
        getString(key)?.let {
            return JSONObject(it)
        }
    }
    catch (e: JSONException) {
        Timber.e("Could not convert $key in $this to JSON.")
    }
    return null
}