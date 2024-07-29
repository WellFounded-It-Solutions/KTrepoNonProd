package com.navigaglobal.mobile.ad.huaweiadskit

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.BannerAdSize
import com.huawei.hms.ads.banner.BannerView
import com.navigaglobal.mobile.ad.huaweiadskit.view.AdWrapperView
import org.json.JSONObject
import se.infomaker.library.AdProvider
import se.infomaker.library.AdProvider2
import se.infomaker.library.OnAdFailedListener
import timber.log.Timber
import kotlin.math.roundToInt

object HuaweiAdsKitViewFactory : AdProvider, AdProvider2 {
    override fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject) =
        getView(context, coreProviderConfig, config, null, null, null)

    override fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject, content: List<JSONObject>?, env: JSONObject?, listener: OnAdFailedListener?): View? {
        try {
            val width = config.optInt("width", 300)
            val height = config.optInt("height", 250)
            val adId = config.getString("adId")

            return AdWrapperView(context).also { wrapperView ->
                wrapperView.layoutParams = ViewGroup.LayoutParams(dp2px(width), dp2px(height))
                BannerView(context).also { adView ->
                    wrapperView.view = adView

                    adView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    adView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

                    adView.adId = adId
                    adView.bannerAdSize = BannerAdSize(width, height)

                    val adParam = AdParam.Builder().build()
                    adView.adListener = object : AdListener() {
                        override fun onAdFailed(p0: Int) {
                            listener?.onAdFailed()
                            Timber.e("Ad failed to load with error code: $p0")
                        }
                    }
                    adView.loadAd(adParam)
                }
            }
        }
        catch (e: Exception) {
            Timber.e(e, "Failed to create request")
            return null
        }
    }
}

private fun dp2px(dp: Int): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}