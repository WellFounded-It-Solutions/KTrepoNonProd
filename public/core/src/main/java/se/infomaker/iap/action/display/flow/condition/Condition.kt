package se.infomaker.iap.action.display.flow.condition

import se.infomaker.frtutilities.meta.ValueProvider

interface Condition {
    /**
     * Evaluate if the condition
     */
    fun evaluate(valueProvider: ValueProvider): Boolean

    fun relevantKeyPaths(): Set<String>
}
