package se.infomaker.livecontentui.extensions

import org.json.JSONException
import org.json.JSONObject

internal fun JSONObject.copy() = JSONObject(toString())

internal fun JSONObject.hasAll(keys: List<String>) = keys.firstOrNull { !has(it) }?.let { false } ?: true

internal fun JSONObject?.patch(another: JSONObject?): JSONObject {
    val out = this ?: JSONObject()
    another?.keys()?.forEach {
        out.put(it, another.get(it))
    }
    return out
}

internal fun JSONObject.safePut(keyPath: String, value: Any) {
    try {
        putAt(keyPath, value)
    }
    catch (e: JSONException) {
        // ignore
    }
}

internal fun JSONObject.putAt(keyPath: String, value: Any?) {
    val parts = keyPath.split(".")
    var parent = this
    parts.forEachIndexed { index, key ->
        if (index == parts.size -1) {
            parent.put(key, value)
            return
        }
        parent.optJSONObject(key)?.let {
            parent = it
        } ?: run {
            parent.put(key, JSONObject().also {
                parent = it
            })
        }
    }
}