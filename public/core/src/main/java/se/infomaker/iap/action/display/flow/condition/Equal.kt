package se.infomaker.iap.action.display.flow.condition

import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.flow.mustachify

open class Equal(val key: String, val value: String) : Condition {
    override fun relevantKeyPaths(): Set<String>  = setOf(key)

    override fun evaluate(valueProvider: ValueProvider): Boolean =
        key.mustachify(valueProvider) == value
}