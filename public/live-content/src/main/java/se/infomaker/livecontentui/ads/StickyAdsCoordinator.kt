package se.infomaker.livecontentui.ads

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.json.JSONObject
import se.infomaker.library.AdViewFactory
import se.infomaker.library.Destroyable
import se.infomaker.library.OnAdFailedListener
import se.infomaker.livecontentui.config.StickyAdsConfig
import se.infomaker.livecontentui.config.providerConfiguration
import timber.log.Timber

class StickyAdsCoordinator(
    private val topAdFrame: FrameLayout,
    private val bottomAdFrame: FrameLayout?,
    private val adProvider: String?,
    private val stickyAdConfig: StickyAdsConfig?
) : DefaultLifecycleObserver {

    private val providerConfig = stickyAdConfig?.providerConfiguration

    private var cachedAdView: View? = null

    override fun onResume(owner: LifecycleOwner) {
        if (cachedAdView != null) return // One ad is enough

        hideAdFrames()
        if (adProvider != null && providerConfig != null) {
            val adView = AdViewFactory.getView(adProvider, topAdFrame.context, providerConfig, emptyList(), JSONObject(), object : OnAdFailedListener {
                override fun onAdFailed() {
                    hideAdFrames2()
                }
            })
            if (adView != null) {
                adView.layoutParams = FrameLayout.LayoutParams(adView.layoutParams.width, adView.layoutParams.height, Gravity.CENTER_HORIZONTAL)
                if (stickyAdConfig?.position == "top") {
                    topAdFrame.addView(adView)
                    topAdFrame.visibility = View.VISIBLE
                }
                else {
                    bottomAdFrame?.addView(adView)
                    bottomAdFrame?.visibility = View.VISIBLE
                }
                cachedAdView = adView
            }
            else {
                hideAdFrames()
                Timber.e("No ad view created for config: $stickyAdConfig")
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        (cachedAdView as? Destroyable)?.destroy()
        cachedAdView = null
        topAdFrame.removeAllViews()
        bottomAdFrame?.removeAllViews()
        hideAdFrames()
    }

    private fun hideAdFrames() {
        topAdFrame.visibility = View.INVISIBLE
        bottomAdFrame?.visibility = View.INVISIBLE
    }

    private fun hideAdFrames2() {
        (cachedAdView as? Destroyable)?.destroy()
        cachedAdView = null
        topAdFrame.removeAllViews()
        bottomAdFrame?.removeAllViews()
        topAdFrame.visibility = View.INVISIBLE
        bottomAdFrame?.visibility = View.INVISIBLE
    }

}