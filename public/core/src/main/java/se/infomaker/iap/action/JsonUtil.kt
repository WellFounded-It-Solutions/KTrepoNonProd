package se.infomaker.iap.action

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.flow.FlowStepHandler

fun JSONObject.flatMapped(keyPath: String): Map<String, String> {
    return Gson().fromJson(this.toString(), JsonObject::class.java).flatMapped(keyPath)
}

fun JsonObject?.flatMapped(keyPath: String, buildUpon: MutableMap<String, String> = mutableMapOf()): Map<String, String> {
    if (this == null) {
        return buildUpon
    }

    this.entrySet().forEach { (key, value) ->
        val path = "$keyPath.$key"
        when {
            value.isJsonPrimitive -> {
                buildUpon.put(path, value.asString)
            }
            value.isJsonObject -> {
                buildUpon.putAll(value.asJsonObject.flatMapped(path, buildUpon))
            }
        }
    }

    return buildUpon
}

fun JSONObject.createOperation(flowStepHandler: FlowStepHandler): Operation {
    val values = flowStepHandler.currentView()?.let {
        flowStepHandler.getValues().flatMapped(it)
    } ?: mapOf()

    return Operation(optString("action"),
            flowStepHandler.getModuleId(),
            optJSONObject("parameters") ?: JSONObject(),
            ActionValueProvider(flowStepHandler.getValueProvider(), values)
    )
}

fun JSONObject.createOperation(currentValueProvider: ValueProvider?, currentModule: String): Operation {
    return Operation(optString("action"),
            optString("moduleId", currentModule),
            optJSONObject("parameters") ?: JSONObject(),
            currentValueProvider
    )
}