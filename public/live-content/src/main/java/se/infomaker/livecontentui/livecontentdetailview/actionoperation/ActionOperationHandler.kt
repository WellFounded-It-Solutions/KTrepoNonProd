package se.infomaker.livecontentui.livecontentdetailview.actionoperation

interface ActionOperationHandler {
    /**
     * Performs an operation, if the operation has a return value it is sent back as a String
     */
    fun perform(operation: Operation): String?

    /**
     * Returns true if the interface can perform the operation
     */
    fun canPerform(operation: Operation): Boolean
}