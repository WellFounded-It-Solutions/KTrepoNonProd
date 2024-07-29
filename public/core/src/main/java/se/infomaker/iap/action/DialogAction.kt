package se.infomaker.iap.action

import android.content.Context
import se.infomaker.iap.action.display.flow.mustachify

object DialogAction : ActionHandler {
    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        operation.parameters.optString("text", null)?.let { text ->
            presentMessageDialog(context, text.mustachify(operation.values)) {
                onResult(Result(true, operation.values))
            }
        }?:kotlin.run {
            onResult(Result(false, operation.values, "Dialog contains no text"))
        }
    }

    override fun canPerform(context: Context, operation: Operation): Boolean {
        return operation.parameters.optString("text", null) != null
    }
}