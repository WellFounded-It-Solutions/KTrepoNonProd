package com.navigaglobal.mobile.ad.vuukle

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import com.navigaglobal.mobile.extension.khaleejtimes.R
import com.vuukle.ads.mediation.Vuukle
import com.vuukle.ads.mediation.adbanner.AdSize
import com.vuukle.ads.mediation.adbanner.BannerAdContainer
import com.vuukle.ads.mediation.adbanner.BannerListener
import org.json.JSONObject
import se.infomaker.frtutilities.ktx.layoutInflater
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.library.AdProvider
import se.infomaker.library.AdProvider2
import se.infomaker.library.OnAdFailedListener
import timber.log.Timber
import kotlin.math.roundToInt

object VuukleAdProvider : AdProvider, AdProvider2 {

    override fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject) =
        getView(context, coreProviderConfig, config, null, null, null)

    override fun getView(
        context: Context,
        coreProviderConfig: JSONObject?,
        config: JSONObject,
        content: List<JSONObject>?,
        state: JSONObject?,
        listener: OnAdFailedListener?
    ): View? {
        //  val vuukleAds = VuukleAdsImpl().also { it.initialize(context.requireActivity()) }
        if (!Vuukle.isSDKInitialized()) {
            Vuukle.initializeSDK(
                context.requireActivity(),
                "be010cdc-dc91-4800-8bb8-946d3be6ca32:9f60b57f-8b98-4b01-a391-60be2dd7a164",
                Vuukle.MASK_BANNER
            )

        }
        try {
            val width = config.optInt("width", 300)
            val height = config.optInt("height", 250)
            val adId = config.getString("adId")
            /*
             * They are forcing us to provide an attribute set, which breaks the "standard contract"
             * of widgets in the Android world.
             */
            val inflated = context.layoutInflater().inflate(R.layout.vuukle_ad_view, null)
            return inflated.findViewById<BannerAdContainer>(R.id.vuukle_ad_view)?.also { adView ->
                adView.layoutParams = FrameLayout.LayoutParams(dp2px(width), dp2px(height))
                //  adView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

                //    adView.loadNextAd()
                //adView.start()
                //adView.setSize(AdSize.BANNER_300x250)
                // AdSize.BANNER_728x90
                //adView.setRefreshInterval(60);

                //  adView.setAdUnitId(adId)
                //val vuukleAdSize = if (height == 50) VuukleAdSize.Type.BANNER else VuukleAdSize.Type.MEDIUM_RECTANGLE
                if (height == 50) adView.setSize(AdSize.BANNER_320x50)
                else
                    adView.setSize(AdSize.BANNER_300x250)
                //Vuukle new listener

                adView.setBannerListener(object : BannerListener {
                    override fun onBannerLoad() {
                    }

                    override fun onBannerFailedToLoadProvider(provider: String?) {
                    }

                    override fun onBannerFailedToLoad() {
//                        adView.updateLayoutParams {
//                        this.height = 0
//                        this.width = 0
//                    }
//                    adView.visibility = View.GONE
//                    listener?.onAdFailed()

                    }

                    override fun onBannerClicked() {

                    }
                })

                // START VUUKLE VOODOO


                //vuukleAds.createBanner(adView)
                //vuukleAds.addErrorListener {
//                    adView.updateLayoutParams {
//                        this.height = 0
//                        this.width = 0
//                    }
//                    adView.visibility = View.GONE
//                    Timber.e(it)
//                    listener?.onAdFailed()
//                }
//                vuukleAds.addResultListener { Timber.d("Fetched demand for $it") }
//                vuukleAds.startAdvertisement()

                // END VUUKLE VOODOO
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create request")
            return null
        }
    }
}

private fun dp2px(dp: Int): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}