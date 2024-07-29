package se.infomaker.iap.push.huawei.di

import android.content.Context
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.navigaglobal.mobile.di.MobileServicesProvider
import com.navigaglobal.mobile.di.MobileServicesProviderKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import se.infomaker.frt.remotenotification.PushRegistrationManager
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.globalConfig
import se.infomaker.iap.push.huawei.HuaweiPushConfig
import se.infomaker.iap.push.huawei.HuaweiPushRegistrationManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PushModule {

    @Binds @IntoMap
    @MobileServicesProviderKey(MobileServicesProvider.HUAWEI)
    abstract fun bindPushRegistrationManager(huaweiPushRegistrationManager: HuaweiPushRegistrationManager): PushRegistrationManager

    companion object {

        @Provides
        @Singleton
        @HuaweiAppId
        fun provideHuaweiAppId(@ApplicationContext context: Context): String {
            return AGConnectServicesConfig.fromContext(context).getString("client/app_id")
        }

        @Provides
        fun provideHuaweiPushConfig(configManager: ConfigManager): HuaweiPushConfig {
            return configManager.globalConfig()
        }
    }
}