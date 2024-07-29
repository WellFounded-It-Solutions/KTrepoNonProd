package com.navigaglobal.mobile.ad.huaweiadskit

import android.content.Context
import com.huawei.hms.ads.HwAds
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.library.AdViewFactory

class HuaweiAdsKitSetup : AbstractInitContentProvider() {

    override fun init(context: Context) {
        HwAds.init(context)
        AdViewFactory.registerAdProvider("HuaweiAdsKit", HuaweiAdsKitViewFactory)
    }
}