package se.infomaker.iap.articleview.item.ad

import android.view.View
import android.view.ViewGroup
import java.lang.ref.WeakReference

class AdViewCache {

    private val cache = mutableMapOf<Int, WeakReference<View>>()
    private val failedViews = mutableSetOf<Int>()

    fun get(position: Int?, adViewCreator: (onAdFailed: () -> Unit) -> View?): View? {
        if (failedViews.contains(position)) return null
        cache[position]?.get()?.let { pooledView ->
            // TODO: Quickfix. This is a symptom of something else...
            if (pooledView.parent == null) {
                return pooledView
            }
            cache.remove(position)
        }
        val adView = adViewCreator {
            position?.let { failedViews.add(it) }
        }
        return adView?.ensureLayoutParams()?.also { view ->
            position?.let { cache[it] = WeakReference(view) }
        }
    }
}

private fun View.ensureLayoutParams(): View {
    if (layoutParams == null) {
        visibility = View.VISIBLE
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    return this
}