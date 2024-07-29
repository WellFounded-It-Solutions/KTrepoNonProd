package com.navigaglobal.mobile.dev.resources.di

import com.navigaglobal.mobile.di.MobileServicesProvider
import com.navigaglobal.mobile.di.MobileServicesProviderKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import se.infomaker.iap.map.MapViewHolderFactory
import se.infomaker.iap.map.NoOpMapViewHolderFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds
    @IntoMap
    @MobileServicesProviderKey(MobileServicesProvider.GOOGLE)
    abstract fun bindGoogleMapViewHolderFactory(googleMapsViewHolder: NoOpMapViewHolderFactory): MapViewHolderFactory

    @Binds
    @IntoMap
    @MobileServicesProviderKey(MobileServicesProvider.HUAWEI)
    abstract fun bindHuaweiMapViewHolderFactory(googleMapsViewHolder: NoOpMapViewHolderFactory): MapViewHolderFactory

    companion object {

        @Provides
        fun provideNoOpMapViewHolderFactory() = NoOpMapViewHolderFactory
    }
}