package com.navigaglobal.mobile.ad.taboola.application

import android.content.Context
import com.navigaglobal.mobile.ad.taboola.*
import com.taboola.android.TBLPublisherInfo
import com.taboola.android.Taboola
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.library.AdViewFactory

class TaboolaAdSetup : AbstractInitContentProvider() {
    override fun init(context: Context) {
        ConfigManager.getInstance(context).getConfig(
            "core",
            TaboolaAdProviderConfigWrapper::class.java
        )?.adProviders?.firstOrNull{it.provider == "Taboola"}?.let {
            Taboola.init(TBLPublisherInfo(it.config.publisherName))
            AdViewFactory.registerAdProvider("Taboola", TaboolaAdProvider())
        }?: throw IllegalArgumentException("Publisher Key Cannot be empty")
    }
}