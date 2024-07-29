package se.infomaker.iap.map.huawei

import se.infomaker.iap.map.MapViewHolderFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HuaweiMapViewHolderFactory @Inject constructor() : MapViewHolderFactory {
    override fun create() = HuaweiMapViewHolder()
}