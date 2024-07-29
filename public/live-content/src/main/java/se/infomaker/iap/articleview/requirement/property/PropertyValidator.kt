package se.infomaker.iap.articleview.requirement.property

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.requirement.RequirementValidator

class PropertyValidator() : RequirementValidator {
    override fun validate(content: ContentStructure, config: JsonObject): Boolean {
        val configuration = Gson().fromJson(config, PropertyValidatorConfig::class.java)

        val value = content.properties.optStringFromKeyPath(configuration.key, null)
        return value == configuration.equals
    }

}

private fun JSONObject.optStringFromKeyPath(keyPath: String, fallback: String?): String? {
    optArrayFromKeyPath(keyPath)?.let {
        if (it.length() > 0) {
            return it.optString(0)
        }
        return fallback
    }
    return fallback
}

private fun JSONObject.optArrayFromKeyPath(keyPath: String): JSONArray? {
    val parts = keyPath.split(".")
    var node = this
    (0..parts.size - 2)
            .asSequence()
            .map { node.optJSONArray(parts[it]) }
            .forEach {
                if (it != null) {
                    if (it.length() > 0) {
                        node = it.getJSONObject(0)
                    } else {
                        return null
                    }
                } else {
                    return null
                }
            }
    return node.optJSONArray(parts[parts.size - 1])
}
