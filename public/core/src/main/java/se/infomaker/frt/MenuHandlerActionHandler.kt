package se.infomaker.frt

import android.content.Context
import se.infomaker.frt.ui.MenuHandler
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result

class MenuHandlerActionHandler(val menuHandler: MenuHandler) :ActionHandler{

    override fun canPerform(context: Context, operation: Operation): Boolean {
        return true
    }

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        operation.getParameter("moduleId")?.let {
            menuHandler.selectModule(it)
            onResult.invoke(Result(true))
        }?:kotlin.run {
            onResult.invoke(Result(false))
        }
    }
}