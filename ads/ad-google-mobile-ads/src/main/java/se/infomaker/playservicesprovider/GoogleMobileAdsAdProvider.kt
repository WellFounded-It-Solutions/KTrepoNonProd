package se.infomaker.playservicesprovider

import android.content.Context
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.library.AdViewFactory
import timber.log.Timber

class GoogleMobileAdsAdProvider : AbstractInitContentProvider() {
    override fun init(context: Context) {
        Timber.d("Initializing GoogleMobileAdsViewFactory")
        AdViewFactory.registerAdProvider("GoogleMobileAds", GoogleMobileAdsViewFactory)
    }
}
