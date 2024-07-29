package se.infomaker.library.keywords

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.frtutilities.JSONUtil
import java.lang.reflect.Type


class KeyWordResolver {
    val gson = Gson()
    private val listType: Type = object : TypeToken<List<KeyWord?>?>() {}.type

    fun resolve(config: JSONObject?, coreConfig: JSONObject?, content: List<JSONObject>?, context: JSONObject?): Map<String,String> {
        val out = mutableMapOf<String, String>()
        coreConfig?.optJSONArray("keywords")?.let {
            val keywords: List<KeyWord> = gson.fromJson(it.toString(), listType)
            for (keyword in keywords) {
                keyword.resolveValue(content, context)?.let { resolved ->
                    out[keyword.key] = resolved
                }
            }
        }
        config?.optJSONArray("keywords")?.let {
            val keywords: List<KeyWord> = gson.fromJson(it.toString(), listType)
            for (keyword in keywords) {
                keyword.resolveValue(content, context)?.let { resolved ->
                    out[keyword.key] = resolved
                }
            }
        }
        return out
    }
}

data class KeyWord(val key: String, val value: String?, val source: String?, val keyPath: String?, val mapping: Map<String, String>?) {

    fun resolveValue(content: List<JSONObject>?, state: JSONObject?): String? {
        return return when(source) {
            "content" -> {
                resolveFirstContent(content)
            }
            "state" -> {
                resolveState(state)
            }
            else ->{
                value
            }
        }
    }

    private fun resolveFirstContent(content: List<JSONObject>?): String? {
        if (keyPath == null) {
            return null
        }
        content?.forEach { item ->
            resolve(item)?.let {
                return it
            }
        }
        return null
    }

    private fun resolve(item: JSONObject): String? {
        if (keyPath == null) {
            return value
        }
        val branches = item.getBranches(keyPath.split("."))
        for (branch in branches) {
            for (i in 0 until branch.length()) {
                branch.optString(i)?.let { leaf ->
                    if (mapping != null) {
                        mapping[leaf]?.let { mappedValue ->
                            return mappedValue
                        }
                    } else {
                        return leaf
                    }
                }
            }
        }
        return value
    }

    private fun resolveState(state: JSONObject?): String? {
        if (state == null) {
            return if (mapping == null) value else null
        }
        JSONUtil.optJSONArray(state, keyPath)?.let {
            for (i in 0 until it.length()) {
                it.optString(i)?.let { value ->
                    if (mapping != null) {
                        if (mapping.containsKey(value)) {
                            return mapping[value]
                        }
                    }
                    else {
                        return value
                    }
                }
            }
        }
        val value = JSONUtil.optString(state, keyPath)
        if (mapping != null) {
            if (mapping.containsKey(value)) {
                return mapping[value]
            }
        }
        else {
            return value
        }
        return null
    }
}

private fun JSONObject.getBranches(keyPath: List<String>) : List<JSONArray> {
    if (keyPath.size == 1) {
        optJSONArray(keyPath[0])?.let {
            return listOf(it)
        }
        return emptyList()
    }
    else {
        val out = mutableListOf<JSONArray>()
        optJSONArray(keyPath[0])?.let {
            val childKeyPath = keyPath.subList(1, keyPath.size)
            for (i in 0 until  it.length()) {
                it.optJSONObject(i)?.let {node ->
                    out.addAll(node.getBranches(childKeyPath))
                }
            }
        }
        return out
    }
}

