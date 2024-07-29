package se.infomaker.iap.articleview.di

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.navigaglobal.mobile.di.MobileServicesProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.infomaker.iap.map.MapViewHolderFactory
import se.infomaker.iap.map.NoOpMapViewHolderFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArticleModule {

    @Provides
    @Singleton
    fun provideMapViewHolderFactory(@ApplicationContext context: Context, factories: Map<MobileServicesProvider, @JvmSuppressWildcards MapViewHolderFactory>): MapViewHolderFactory {
        val googleApiResult = GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context)
        val factory = if (googleApiResult == ConnectionResult.SERVICE_INVALID) {
            factories[MobileServicesProvider.HUAWEI]
        } else {
            factories[MobileServicesProvider.GOOGLE]
        }
        return factory ?: run {
            if (factories.isEmpty()) {
                throw NullPointerException("No valid map dependency installed.")
            }
            else {
                Timber.e("No valid map dependency installed, running no-op version. Google Api connection result: [$googleApiResult]")
            }
            NoOpMapViewHolderFactory
        }
    }
}