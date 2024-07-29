package se.infomaker.playservicesprovider

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.json.JSONObject
import se.infomaker.library.AdProvider
import se.infomaker.library.AdProvider2
import se.infomaker.library.Bid
import se.infomaker.library.BidManager
import se.infomaker.library.Destroyable
import se.infomaker.library.OnAdFailedListener
import se.infomaker.library.keywords.KeyWordResolver
import timber.log.Timber
import kotlin.math.roundToInt


object GoogleMobileAdsViewFactory : AdProvider, AdProvider2 {
    private val keyWordResolver = KeyWordResolver()

    override fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject) =
            getView(context, coreProviderConfig, config, null, null, null)

    override fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject, content: List<JSONObject>?, state: JSONObject?, listener: OnAdFailedListener?): View? {
        try {
            val width = config.optInt("width", 320)
            val height = config.optInt("height", 320)
            val adId = config.getString("adId")

            return AdWrapperView(context).also { wrapperView ->
                wrapperView.layoutParams = ViewGroup.LayoutParams(dp2px(width), dp2px(height))
                AdManagerAdView(context).also { adView ->
                    wrapperView.view = adView

                    adView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    adView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

                    adView.adUnitId = adId

                    BidManager.getBid(adId) { bid ->
                        bid?.onConsumed?.invoke()
                        bid?.applyTo(adView) ?: adView.resize(width, height)

                        val adRequest = AdManagerAdRequest.Builder()
                                .addKeyWords(keyWordResolver.resolve(config, coreProviderConfig, content, state))
                                .addBid(bid)
                                .build()
                        adView.adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                bid?.applyTo(adView)
                            }

                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                listener?.onAdFailed()
                            }
                        }
                        adView.loadAd(adRequest)
                    }
                }
            }
        }
        catch (e: Exception) {
            Timber.e(e, "Failed to create request")
            return null
        }
    }
}

private fun Bid.applyTo(view: AdManagerAdView) = size?.let { view.resize(it.width, it.height) }

private fun AdManagerAdView.resize(width: Int, height: Int) {
    val adSize = AdSize(width, height)
    setAdSizes(adSize)

    (parent as? AdWrapperView)?.let {
        it.updateLayoutParams {
            this.width = dp2px(width)
            this.height = dp2px(height)
        }
    }
}

private fun AdManagerAdRequest.Builder.addKeyWords(keyWords: Map<String, String>): AdManagerAdRequest.Builder {
    for (entry in keyWords.entries) {
        keyWords.forEach { addCustomTargeting(it.key, it.value) }
    }
    return this
}

private fun AdManagerAdRequest.Builder.addBid(bid: Bid?): AdManagerAdRequest.Builder {
    bid?.demand?.forEach { addCustomTargeting(it.key, it.value) }
    return this
}

private fun View.hasLayoutParams() = layoutParams != null

private class AdWrapperView(context: Context) : FrameLayout(context), DefaultLifecycleObserver, Destroyable {

    var view: AdManagerAdView? = null
        set(value) {
            value?.let { addView(it) }
            field = value
        }

    override fun onResume(owner: LifecycleOwner) {
        view?.resume()
    }

    override fun onPause(owner: LifecycleOwner) {
        view?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        destroy()
    }

    override fun destroy() {
        view?.destroy()
        view = null
    }
}

private fun dp2px(dp: Int): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}