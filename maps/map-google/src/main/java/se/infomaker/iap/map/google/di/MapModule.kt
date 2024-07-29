package se.infomaker.iap.map.google.di

import com.navigaglobal.mobile.di.MobileServicesProvider
import com.navigaglobal.mobile.di.MobileServicesProviderKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import se.infomaker.iap.map.MapViewHolderFactory
import se.infomaker.iap.map.google.GoogleMapsViewHolderFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds @IntoMap
    @MobileServicesProviderKey(MobileServicesProvider.GOOGLE)
    abstract fun bindMapViewHolderFactory(googleMapsViewHolder: GoogleMapsViewHolderFactory): MapViewHolderFactory
}