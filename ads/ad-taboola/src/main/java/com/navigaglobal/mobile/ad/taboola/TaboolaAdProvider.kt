package com.navigaglobal.mobile.ad.taboola

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import com.taboola.android.TBLClassicPage
import com.taboola.android.TBLClassicUnit
import com.taboola.android.Taboola
import com.taboola.android.annotations.TBL_PLACEMENT_TYPE
import com.taboola.android.listeners.TBLClassicListener
import org.json.JSONObject
import se.infomaker.library.*
import se.infomaker.library.keywords.KeyWordResolver
import timber.log.Timber
import kotlin.math.roundToInt

class TaboolaAdProvider: AdProvider, AdProvider2 {
    override fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject) =
        getView(context, coreProviderConfig, config, null, null, null)

    override fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject, content: List<JSONObject>?, state: JSONObject?, listener: OnAdFailedListener?): View? {
        try {
            val width = config.optInt("width", 320)
            val properties= TaboolaAdPlacementProperties(config.getString("placementName"),
                                                                config.getString("pageType"),
                                                                config.getString("pageUrl"),
                                                                config.getString("targetType"),
                                                                config.getString("mode"))

            val tblClassicPage= getTaboolaClassicPage(properties)
            return  getTaboolaUnitView(context,properties,tblClassicPage).also {
                    adview->
                adview.layoutParams = ViewGroup.LayoutParams(dp2px(width), ViewGroup.LayoutParams.MATCH_PARENT)

                adview.fetchContent()
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


/**
 * Defining a Page that represents Taboola Page
 * Notice: A Unit of unlimited items, called "Feed" in Taboola, can be set in TBL_PLACEMENT_TYPE.PAGE_BOTTOM only.
 */
private fun getTaboolaClassicPage(properties: TaboolaAdPlacementProperties):TBLClassicPage{
    // Define a page to control all Unit placements on this screen
    val classicPage: TBLClassicPage =
        Taboola.getClassicPage(properties.pageUrl, properties.pageType)
return  classicPage
}
/**
 * get a Unit from Taboola Page then We will add it to screen and fetch its content
 * Notice: A Unit of unlimited items, called "Feed" in Taboola, can be set in TBL_PLACEMENT_TYPE.PAGE_BOTTOM only.
 */
private fun getTaboolaUnitView(context: Context,properties: TaboolaAdPlacementProperties,classicPage: TBLClassicPage):TBLClassicUnit{

    // Define a single Unit to display
    val classicUnit: TBLClassicUnit = classicPage.build(
        context,
        properties.placementName,
        properties.mode,
        TBL_PLACEMENT_TYPE.PAGE_BOTTOM,
        object : TBLClassicListener() {
            override fun onAdReceiveSuccess() {
                super.onAdReceiveSuccess()
                println("Taboola | onAdReceiveSuccess")
            }

            override fun onAdReceiveFail(error: String?) {
                super.onAdReceiveFail(error)
                println("Taboola | onAdReceiveFail: $error")
            }
        })

    return classicUnit
}


