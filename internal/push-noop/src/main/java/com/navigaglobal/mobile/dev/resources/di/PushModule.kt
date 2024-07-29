package com.navigaglobal.mobile.dev.resources.di

import com.navigaglobal.mobile.di.MobileServicesProvider
import com.navigaglobal.mobile.di.MobileServicesProviderKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import se.infomaker.frt.remotenotification.NoOpPushRegistrationManager
import se.infomaker.frt.remotenotification.PushRegistrationManager

@Module
@InstallIn(SingletonComponent::class)
abstract class PushModule {

    @Binds @IntoMap
    @MobileServicesProviderKey(MobileServicesProvider.GOOGLE)
    abstract fun bindGooglePushRegistrationManager(noopPushRegistrationManager: NoOpPushRegistrationManager): PushRegistrationManager

    @Binds @IntoMap
    @MobileServicesProviderKey(MobileServicesProvider.HUAWEI)
    abstract fun bindHuaweiPushRegistrationManager(noopPushRegistrationManager: NoOpPushRegistrationManager): PushRegistrationManager

    companion object {

        @Provides
        fun provideNoOpPushRegistrationManager() = NoOpPushRegistrationManager
    }
}