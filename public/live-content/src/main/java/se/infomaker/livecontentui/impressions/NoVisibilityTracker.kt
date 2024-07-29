package se.infomaker.livecontentui.impressions

import android.view.View

class NoVisibilityTracker :VisibilityTracker {
    override fun resume() {}

    override fun pause() {}

    override fun resetIfChanged(view: View, tracker: ContentTracker?) {}

}