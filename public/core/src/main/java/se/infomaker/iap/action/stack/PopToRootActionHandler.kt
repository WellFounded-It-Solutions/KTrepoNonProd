package se.infomaker.iap.action.stack

import android.content.Context
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result

object PopToRootActionHandler : ActionHandler {

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        val stackPopper = StackPopper(onResult)
        stackPopper.popToRoot(context.requireActivity())
    }

    override fun canPerform(context: Context, operation: Operation) = true
}