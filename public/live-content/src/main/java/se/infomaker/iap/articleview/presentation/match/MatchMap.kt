package se.infomaker.iap.articleview.presentation.match

import org.json.JSONObject
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.iap.articleview.util.containsAny

typealias MatchMap = Map<String, List<String>>

fun MatchMap.matches(content: JSONObject, context: JSONObject?): Boolean {
    return all { entry ->
        val keyPath = entry.key.split(".").drop(1).joinToString(".")
        when {
            entry.key.startsWith("content") -> {
                JSONUtil.optJSONArray(content, keyPath)?.containsAny(entry.value) ?: false
            }
            entry.key.startsWith("context") -> {
                context?.let { JSONUtil.optString(it, keyPath, null) in entry.value } ?: false
            }
            else -> false
        }
    }
}