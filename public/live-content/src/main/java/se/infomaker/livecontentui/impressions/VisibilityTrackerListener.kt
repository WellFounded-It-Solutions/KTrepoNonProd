package se.infomaker.livecontentui.impressions

import android.view.View

interface VisibilityTrackerListener {
    fun onVisibilityChanged(visibleViews: List<View>, invisibleViews: List<View>)
}