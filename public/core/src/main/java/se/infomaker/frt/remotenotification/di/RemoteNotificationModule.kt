package se.infomaker.frt.remotenotification.di

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.navigaglobal.mobile.di.MobileServicesProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.infomaker.frt.moduleinterface.ModuleIntegration
import se.infomaker.frt.remotenotification.NoOpPushRegistrationManager
import se.infomaker.frt.remotenotification.NotificationFilter
import se.infomaker.frt.remotenotification.OnRemoteNotificationListener
import se.infomaker.frt.remotenotification.OnRemoteNotificationListenerFactory
import se.infomaker.frt.remotenotification.PushRegistrationManager
import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteNotificationModule {

    @Provides
    @Singleton
    fun provideNotificationHandlers(@ApplicationContext context: Context, moduleIntegrations: List<@JvmSuppressWildcards ModuleIntegration>): Map<String, OnNotificationInteractionHandler> {
        return moduleIntegrations.mapNotNull { integration ->
            (integration as? OnRemoteNotificationListenerFactory)?.let { factory ->
                integration.id to factory.createNotificationHandler(context, integration.id)
            }
        }.toMap()
    }

    @Provides
    @Singleton
    fun provideDefaultListenerWithFiltersRegister(@ApplicationContext context: Context, moduleIntegrations: List<@JvmSuppressWildcards ModuleIntegration>): LinkedHashMap<OnRemoteNotificationListener, Set<NotificationFilter>> {
        val out = LinkedHashMap<OnRemoteNotificationListener, Set<NotificationFilter>>()
        moduleIntegrations.forEach { integration ->
            (integration as? OnRemoteNotificationListenerFactory)?.let { factory ->
                val listener = factory.create(context, integration.id)
                val filters = factory.createFilters(context, integration.id).toHashSet()
                out[listener] = filters
            }
        }
        return out
    }

    @Provides
    @Singleton
    fun providePushRegistrationManager(@ApplicationContext context: Context, managers: Map<MobileServicesProvider, @JvmSuppressWildcards PushRegistrationManager>): PushRegistrationManager {
        val googleApiResult = GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context)
        val manager = if (googleApiResult == ConnectionResult.SERVICE_INVALID) {
            managers[MobileServicesProvider.HUAWEI]
        } else {
            managers[MobileServicesProvider.GOOGLE]
        }
        return manager ?: run {
            if (managers.isEmpty()) {
                throw NullPointerException("No valid push dependency installed.")
            }
            else {
                Timber.e("No valid push dependency installed, running no-op version. Google Api connection result: [$googleApiResult]")
            }
            NoOpPushRegistrationManager
        }
    }
}