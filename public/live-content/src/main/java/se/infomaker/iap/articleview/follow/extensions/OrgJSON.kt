package se.infomaker.iap.articleview.follow.extensions

import org.json.JSONArray
import org.json.JSONObject

internal inline fun JSONArray.find(predicate: (JSONObject) -> Boolean): JSONObject? {
    return firstOrNull(predicate)
}

internal inline fun JSONArray.firstOrNull(predicate: (JSONObject) -> Boolean): JSONObject? {
    forEach { if (predicate(it)) return it }
    return null
}

internal inline fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    (0 until length()).forEach { action(getJSONObject(it)) }
}

internal inline fun JSONArray.forEachString(action: (String) -> Unit) {
    (0 until length()).forEach { action(getString(it)) }
}

internal inline fun <T> JSONArray.mapStringNotNull(transform: (String) -> T?): List<T> {
    return mapStringNotNullTo(ArrayList<T>(), transform)
}

internal inline fun <T> JSONArray.mapStringNotNullTo(destination: MutableList<T>, transform: (String) -> T?): List<T> {
    forEachString { element -> transform(element)?.let { destination.add(it) } }
    return destination
}

internal fun String?.wrapInJSON(): JSONArray? {
    return try { JSONArray().also { it.put(this) } } catch (e: Exception) { null }
}