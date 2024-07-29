package se.infomaker.iap.action.display.flow.view

import se.infomaker.frtutilities.meta.ValueProvider

interface ValueView {
    /**
     * Sets a value to the view
     */
    fun setValue(values: String)

    /**
     * Gets a value from the view
     */
    fun getValue(): String

    /**
     * Validate the view using view validators
     */
    fun validate(valueProvider: ValueProvider): Boolean
}