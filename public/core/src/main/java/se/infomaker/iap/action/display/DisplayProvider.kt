package se.infomaker.iap.action.display


import androidx.fragment.app.Fragment
import se.infomaker.iap.action.Operation

/**
 * Creates a display for an operation
 */
interface DisplayProvider {
    /**
     * Create an instance of the display for the operation
     */
    fun create(operation: Operation): androidx.fragment.app.Fragment
}