package se.infomaker.iap.action.module

import android.content.Context
import se.infomaker.frt.moduleinterface.action.module.Module.MODULE_ID
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import timber.log.Timber

object ModuleActionHandler : ActionHandler {
    override fun canPerform(context: Context, operation: Operation): Boolean = Module.isValid(operation.getParameter(ModuleActivity.MODULE_NAME) ?: "")

    override fun isLongRunning(): Boolean = false

    override fun perform(context: Context, operation: Operation, onResult: Function1<Result, Unit>) {
        val bundle = operation.parametersAsBundle()
        // Module is defined by id externally but moduleId internally -> remapping
        if (!bundle.containsKey(MODULE_ID)) {
            bundle.putString(MODULE_ID, bundle.getString("id"))
        }
        try {
            operation.getParameter(ModuleActivity.MODULE_NAME)?.let { name ->
                if (name.isNullOrEmpty()) {
                    onResult.invoke(Result(false, operation.values, "No module name"))
                } else {
                    Module.open(context, name, bundle)
                    onResult.invoke(Result(true, operation.values, null))
                }
            }

        } catch (e: InvalidModuleException) {
            Timber.e(e, "Trying to open invalid module")
            onResult.invoke(Result(false, operation.values, "Trying to open invalid module"))
        }
    }
}
