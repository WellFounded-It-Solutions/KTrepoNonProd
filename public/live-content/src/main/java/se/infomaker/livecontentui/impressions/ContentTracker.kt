package se.infomaker.livecontentui.impressions

interface ContentTracker {
    /**
     * Called when the view tracker is shown
     */
    fun register()

    /**
     * @return true if the other tracks the same content
     */
    fun isSameAs(other: ContentTracker?): Boolean
}