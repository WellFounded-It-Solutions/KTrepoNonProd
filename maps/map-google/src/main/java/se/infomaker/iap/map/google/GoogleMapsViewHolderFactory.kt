package se.infomaker.iap.map.google

import se.infomaker.iap.map.MapViewHolderFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsViewHolderFactory @Inject constructor() : MapViewHolderFactory {
    override fun create() = GoogleMapsViewHolder()
}