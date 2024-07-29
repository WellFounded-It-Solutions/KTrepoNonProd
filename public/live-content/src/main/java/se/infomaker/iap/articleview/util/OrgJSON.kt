package se.infomaker.iap.articleview.util

import org.json.JSONArray
import org.json.JSONObject

internal fun JSONArray.asStringList(): List<String> {
    val destination = mutableListOf<String>()
    for (i in 0 until length()) {
        optString(i, null)?.let { destination.add(it) }
    }
    return destination
}

internal fun JSONArray.containsAny(values: List<String>) = asStringList().containsAny(values)