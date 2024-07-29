package se.infomaker.iap.articleview.item.ad

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import se.infomaker.iap.articleview.view.FocusState
import se.infomaker.library.AdViewFactory
import se.infomaker.library.OnAdFailedListener
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils
import se.infomaker.livecontentui.section.ads.AdStateManager
import timber.log.Timber

class AdViewProvider {

    private val adViewCache = AdViewCache()

    fun provideView(parent: ViewGroup, lifecycle: Lifecycle?, item: AdItem, position: Int?, adListener: OnAdFailedListener): View? {
        val context = parent.context
        val adConfiguration = item.adConfiguration.toJSONObject()
        return when(item.focusState) {
            FocusState.OUT_OF_FOCUS, FocusState.BLOCKED -> {
                Timber.d("Skipping ad, out of focus or blocked.")
                FrameLayout(context).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        DefaultUtils.dp2px(context, adConfiguration.optInt("width", 320)),
                        DefaultUtils.dp2px(context, adConfiguration.optInt("height", 320))
                    )
                }
            }
            FocusState.IN_FOCUS -> {
                adViewCache.get(position) { onAdFailed ->
                    AdViewFactory.getView(item.adService, context, adConfiguration, listOf(item.content), AdStateManager.get(context), object : OnAdFailedListener {
                        override fun onAdFailed() {
                            onAdFailed()
                            adListener.onAdFailed()
                        }
                    }).apply {
                        (this as? LifecycleObserver)?.let {
                            lifecycle?.addObserver(it)
                        }
                    }
                }
            }
        }.exhaustive()
    }
}

private fun JsonObject.toJSONObject() = try {
    JSONObject(toString())
}
catch (e: JSONException) {
    JSONObject()
}

private fun <T> T.exhaustive() = this