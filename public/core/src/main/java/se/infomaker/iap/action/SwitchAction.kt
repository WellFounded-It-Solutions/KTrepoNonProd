package se.infomaker.iap.action

import android.content.Context
import org.json.JSONObject
import se.infomaker.iap.action.display.flow.mustachify

object SwitchAction : ActionHandler {
    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        val cases = operation.parameters.cases()
        val case = cases[operation.parameters.value()?.mustachify(operation.values)] ?: cases["default"]

        if(case == null) {
            onResult(Result(success = true, value = operation.values))
        } else {
            case.createOperation(operation.values, operation.moduleID ?: "global").perform(context, onResult)
        }
    }

    override fun canPerform(context: Context, operation: Operation): Boolean = true
}

private fun JSONObject.value(): String? = optString("value", null)

private fun JSONObject.cases(): Map<String, JSONObject> {
    return optJSONObject("case")?.let { case ->
        return@let case.keys().asSequence().toList().mapNotNull {
            (case[it] as? JSONObject)?.let { case ->
                it to case
            }
        }.toMap()
    } ?: mapOf()
}
