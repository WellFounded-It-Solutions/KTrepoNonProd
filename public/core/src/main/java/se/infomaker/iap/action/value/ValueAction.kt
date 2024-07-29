package se.infomaker.iap.action.value

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import se.infomaker.frtutilities.GlobalValueManager
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.iap.action.display.flow.mustachify
import timber.log.Timber

/**
 * Persist values and make them accessible using global kvalue provider
 */
object ValueAction : ActionHandler {
    private var values : JSONObject? = null

    fun load(context: Context) {
        val preferences = context.getSharedPreferences("ValueAction", Context.MODE_PRIVATE)
        preferences.getString("values", null)?.let {
            try {
                values = JSONObject(it)
                values?.let {
                    GlobalValueManager.put("GLOBAL", it)
                }
            }
            catch (e: JSONException) {
                Timber.e(e, "Failed to load values")
            }
        }
    }

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        when (operation.action) {
            "set-values" -> {

                val resolvedValues = operation.parameters.mustachify(operation.values)
                values?.let {
                    it.update(resolvedValues)
                    update(context, it)
                } ?:kotlin.run {
                    update(context, resolvedValues)
                }
                Timber.d("Values updated")
                onResult.invoke(Result(true, operation.values))
            }
            else -> {
                onResult.invoke(Result(false, operation.values, "Unsupported action ${operation.action}"))
            }
        }
    }

    private fun update(context: Context, updated: JSONObject) {
        values = updated
        GlobalValueManager.put("GLOBAL", updated)
        context.getSharedPreferences("ValueAction", Context.MODE_PRIVATE)
                .edit()
                .putString("values", values.toString())
                .apply()
    }

    override fun canPerform(context: Context, operation: Operation): Boolean = true
}

private fun JSONObject.update(jsonObject: JSONObject) {
    jsonObject.keys().forEach { key ->
        val child = jsonObject.optJSONObject(key)
        if (child != null) {
            var currentChild = optJSONObject(key)
            if (currentChild == null) {
                currentChild = JSONObject()
            }
            currentChild.update(child)
        }
        else {
            jsonObject.opt(key)?.let { value ->
                put(key, value)
            } ?: kotlin.run {
                remove(key)
            }
        }
    }
}
