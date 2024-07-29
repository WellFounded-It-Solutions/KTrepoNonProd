package se.infomaker.livecontentui.livecontentrecyclerview.adapter

import android.view.View
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

class AdPosition(val adIndex: Int, val configuration: JSONObject) {

    val isFailure: Boolean
        get() = failed.contains(adIndex)

    fun register(view: View) {
        views[adIndex]?.clear()
        views[adIndex] = WeakReference(view)
    }

    fun current(): View? {
        return views[adIndex]?.get()
    }

    fun markFailed() {
        views[adIndex]?.clear()
        failed.add(adIndex)
    }

    companion object {
        private val views = mutableMapOf<Int, WeakReference<View>>()
        private val nextIndex = AtomicInteger(0)
        private val failed = mutableSetOf<Int>()

        @JvmStatic
        fun create(configuration: JSONObject): AdPosition {
            return AdPosition(nextIndex.getAndIncrement(), configuration)
        }
    }
}