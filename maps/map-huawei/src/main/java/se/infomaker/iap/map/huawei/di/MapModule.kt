package se.infomaker.iap.map.huawei.di

import com.navigaglobal.mobile.di.MobileServicesProvider
import com.navigaglobal.mobile.di.MobileServicesProviderKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import se.infomaker.iap.map.MapViewHolderFactory
import se.infomaker.iap.map.huawei.HuaweiMapViewHolderFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds @IntoMap
    @MobileServicesProviderKey(MobileServicesProvider.HUAWEI)
    abstract fun bindMapViewHolderFactory(factory: HuaweiMapViewHolderFactory): MapViewHolderFactory
}