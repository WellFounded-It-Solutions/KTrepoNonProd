package se.infomaker.iap.action

import android.content.Context

interface ActionHandler {

    fun isLongRunning(): Boolean = false

    /**
     * Performs an operation, if the operation has a return value it is sent back as a String
     */
    fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit)

    /**
     * Returns true if the interface can perform the operation
     */
    fun canPerform(context: Context, operation: Operation): Boolean
}
