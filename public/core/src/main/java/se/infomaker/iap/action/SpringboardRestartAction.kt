package se.infomaker.iap.action

import android.content.Context
import se.infomaker.iap.SpringBoardManager

object SpringboardRestartAction : ActionHandler {
    override fun isLongRunning(): Boolean {
        return false
    }

    override fun perform(context: Context, operation: Operation, onResult: Function1<Result, Unit>) {
        SpringBoardManager.restart(context)
        onResult.invoke(Result(true, operation.values))
    }

    override fun canPerform(context: Context, operation: Operation): Boolean {
        return true
    }
}