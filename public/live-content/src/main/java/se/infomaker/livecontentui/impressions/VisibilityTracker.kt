package se.infomaker.livecontentui.impressions

import android.view.View
import androidx.annotation.NonNull

interface VisibilityTracker {
    fun resume()
    fun pause()
    fun resetIfChanged(@NonNull view: View, tracker: ContentTracker?)
}
