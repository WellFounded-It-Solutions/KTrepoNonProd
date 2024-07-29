package se.infomaker.iap.action

import android.content.Context
import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider
import timber.log.Timber

object SequenceAction : ActionHandler {
    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        val actions = operation.parameters.actions().iterator()

        if (actions.hasNext()) {
            actions.next().createRecursiveOperation(context, operation.moduleID ?: "global", operation.values, actions, onResult)
        }
        else {
            onResult.invoke(Result(false, operation.values, "No actions defined"))
        }
    }

    private fun JSONObject.createRecursiveOperation(context: Context, moduleId: String,  values: ValueProvider?, actions: Iterator<JSONObject>, onResult: (Result) -> Unit) {
        val operation = createOperation(values, moduleId)
        operation.perform(context) { result ->
            if (result.success) {
                if (actions.hasNext()) {
                    actions.next().createRecursiveOperation(context, moduleId,result.value ?: values, actions, onResult)
                } else {
                    onResult(Result(true, result.value))
                }
            } else {
                Timber.w("Failed to perform operation " + operation)
                onResult(Result(false, result.value, "Could not perform all actions in the sequence."))
            }
        }
    }

    override fun canPerform(context: Context, operation: Operation): Boolean =  true
}

private fun JSONObject.actions(): List<JSONObject> {
    return optJSONArray("actions")?.let { actions ->
        (0..actions.length())
                .mapNotNull { actions.optJSONObject(it) }
    } ?: listOf()
}
