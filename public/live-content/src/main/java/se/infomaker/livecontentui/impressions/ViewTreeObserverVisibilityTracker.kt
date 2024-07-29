package se.infomaker.livecontentui.impressions

import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.NonNull
import timber.log.Timber
import java.util.WeakHashMap


class ViewTreeObserverVisibilityTracker(private val viewTreeObserver: ViewTreeObserver) : VisibilityTracker, ViewTreeObserver.OnPreDrawListener {

    override fun onPreDraw(): Boolean {
        impressionTracker.scheduleVisibilityCheck()
        return true
    }

    companion object {
        private const val VISIBILITY_CHECK_DELAY_MILLIS: Long = 500
        private const val MINIMAL_VISIBLE_PERCENT = 10
    }

    private val trackedViews = WeakHashMap<View, ContentTracker>()

    private var impressionTracker = ImpressionTracker()
    private var visibilityChecker = VisibilityChecker(MINIMAL_VISIBLE_PERCENT)

    override fun resume() {
        impressionTracker.clear()
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnPreDrawListener(this)
        } else {
            Timber.w("Visibility tracker root view is not alive")
        }
    }

    override fun pause() {
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.removeOnPreDrawListener(this)
        } else {
            Timber.w("Visibility tracker root view is not alive")
        }
    }

    override fun resetIfChanged(@NonNull view: View, tracker: ContentTracker?) {
        trackedViews[view]?.let {
            if (it.isSameAs(tracker)) {
                return
            }
            else {
                Timber.d("Not the same")
            }
        }
        trackedViews[view] = tracker

        impressionTracker.reset(view)
        impressionTracker.scheduleVisibilityCheck()
    }

    inner class ImpressionTracker : Runnable {
        private var isVisibilityCheckScheduled: Boolean = false
        private var handler = Handler()
        private val visibleViews: MutableList<View> = mutableListOf()
        private val lastVisibleViews: MutableList<View> = mutableListOf()

        override fun run() {
            isVisibilityCheckScheduled = false
            for (entry in trackedViews.entries) {
                val view = entry.key
                val tracker = entry.value
                if (view.isAttachedToWindow && visibilityChecker.isVisible(view)) {
                    visibleViews.add(view)
                    if (!lastVisibleViews.contains(view)) {
                        tracker?.register()
                    }
                }
            }

            lastVisibleViews.clear()
            lastVisibleViews.addAll(visibleViews)
            visibleViews.clear()
        }

        fun reset(view: View) {
            lastVisibleViews.remove(view)
        }

        fun scheduleVisibilityCheck() {
            if (isVisibilityCheckScheduled) {
                return
            }
            isVisibilityCheckScheduled = true
            handler.postDelayed(impressionTracker, VISIBILITY_CHECK_DELAY_MILLIS)
        }

        fun clear() {
            lastVisibleViews.clear()
        }
    }
}

