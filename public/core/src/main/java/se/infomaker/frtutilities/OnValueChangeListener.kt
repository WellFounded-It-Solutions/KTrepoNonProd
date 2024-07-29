package se.infomaker.frtutilities

interface OnValueChangeListener {
    /**
     * Called when a value is changed
     * @return true if still interested in value updates
     */
    fun onValueChanged(): Boolean
}