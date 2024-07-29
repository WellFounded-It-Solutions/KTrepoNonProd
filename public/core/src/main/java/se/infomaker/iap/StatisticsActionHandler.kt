package se.infomaker.iap

import android.content.Context
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.frtutilities.TextUtils
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.iap.action.display.flow.mustachify

object StatisticsActionHandler : ActionHandler {
    override fun canPerform(context: Context, operation: Operation): Boolean = true

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        val builder = StatisticsEvent.Builder()

        if (!TextUtils.isEmpty(operation.moduleID)) {
            builder.moduleId(operation.moduleID)
            val moduleName = ModuleInformationManager.getInstance().getModuleName(operation.moduleID)
            if (!moduleName.isNullOrEmpty()) {
                builder.moduleName(moduleName)
            }
            val title = ModuleInformationManager.getInstance().getModuleTitle(operation.moduleID)
            if (!title.isNullOrEmpty()) {
                builder.moduleTitle(title)
            }
        }
        operation.parameters.keys().forEach {
            val value = operation.getParameter(it)?.mustachify(operation.values)
            if (it == "eventName") {
                builder.event(value)
            }
            else {
                builder.attribute(it, value)
            }
        }

        StatisticsManager.getInstance().logEvent(builder.build())

        onResult.invoke(Result(true, operation.values))
    }

}