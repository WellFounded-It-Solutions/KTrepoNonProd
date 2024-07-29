package se.infomaker.iap.push.google.di

import com.navigaglobal.mobile.di.IsDebuggable
import com.navigaglobal.mobile.di.MobileServicesProvider
import com.navigaglobal.mobile.di.MobileServicesProviderKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import se.infomaker.frt.remotenotification.PushRegistrationManager
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.globalConfig
import se.infomaker.iap.push.google.FirebasePushConfig
import se.infomaker.iap.push.google.FirebasePushRegistrationManager
import se.infomaker.iap.push.google.api.RegistrationService
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PushModule {

    @Binds @IntoMap
    @MobileServicesProviderKey(MobileServicesProvider.GOOGLE)
    abstract fun bindPushRegistrationManager(firebasePushRegistrationManager: FirebasePushRegistrationManager): PushRegistrationManager

    companion object {

        @Provides
        fun provideFirebasePushConfig(configManager: ConfigManager): FirebasePushConfig {
            return configManager.globalConfig()
        }

        @Provides
        @PushRegistrationLoggingInterceptor
        fun providePushRegistrationLoggingInterceptor(@IsDebuggable isDebuggable: Boolean): HttpLoggingInterceptor? {
            return if (isDebuggable) {
                HttpLoggingInterceptor { message ->
                    Timber.tag("PushBackend")
                    Timber.d(message)
                }.also { it.level = HttpLoggingInterceptor.Level.BODY }
            }
            else null
        }
        
        @Provides
        @Singleton
        @PushRegistrationOkHttpClient
        fun providePushRegistrationOkHttpClient(okHttpClient: OkHttpClient, @PushRegistrationLoggingInterceptor httpLoggingInterceptor: HttpLoggingInterceptor?): OkHttpClient {
            val builder = okHttpClient.newBuilder()

            httpLoggingInterceptor?.let {
                builder.addNetworkInterceptor(it)
            }

            return builder.build()
        }

        @Provides
        @Singleton
        fun providePushRegistrationService(@PushRegistrationOkHttpClient okHttpClient: OkHttpClient): RegistrationService {
            return Retrofit.Builder()
                .baseUrl("https://infomaker.io/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create()
        }
    }
}