package se.infomaker.iap.action

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.frtutilities.meta.ValueProvider

data class Operation(val action: String, val moduleID: String?, val parameters: JSONObject, val values: ValueProvider?) {
    companion object {
        private val gson = Gson()

        @JvmStatic
        @JvmOverloads
        fun JsonObject.create(moduleID: String? = null, valueProvider: ValueProvider?): Operation {
            val parameters = getAsJsonObject("parameters")
            val moduleId = moduleID ?: when {
                has("moduleId") -> {
                    getAsJsonPrimitive("moduleId").asString
                }
                parameters.has("moduleId") -> {
                    parameters.getAsJsonPrimitive("moduleId").asString
                }
                else -> null
            }
            return Operation(
                    action = getAsJsonPrimitive("action").asString,
                    moduleID = moduleId,
                    parameters = JSONObject(parameters.toString()),
                    values = valueProvider)
        }
    }

    private fun parameters(): Iterator<String>? {
        return parameters.keys()
    }

    fun getParameter(keyPath: String): String? {
        return JSONUtil.getString(parameters, keyPath)
    }

    fun perform(context: Context, onResult: (Result) -> Unit) {
        ActionManager.perform(context, this, onResult)
    }

    /**
     * Get parameters as a concrete class
     *
     * @return concrete class of T
     */
    fun <T> parametersAs(clazz: Class<T>): T = gson.fromJson(parameters.toString(), clazz)

    fun parametersAsBundle(): Bundle {
        val bundle = Bundle()
        parameters()?.forEach {
            bundle.putString(it, getParameter(it))
        }
        return bundle
    }
}