package se.infomaker.iap.action.display.flow.condition

import se.infomaker.frtutilities.meta.ValueProvider

class NotEqual(key: String, value: String): Equal(key, value) {
    override fun evaluate(valueProvider: ValueProvider): Boolean {
        return !super.evaluate(valueProvider)
    }
}