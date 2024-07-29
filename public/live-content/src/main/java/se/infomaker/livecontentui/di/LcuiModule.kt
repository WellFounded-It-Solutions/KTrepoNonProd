package se.infomaker.livecontentui.di

import com.navigaglobal.mobile.di.IsDebuggable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import se.infomaker.livecontentui.common.di.GlobalLiveContentUiConfig
import se.infomaker.livecontentui.config.LiveContentUIConfig
import se.infomaker.livecontentui.sharing.SharingService
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LcuiModule {

    @Provides
    @SharingLoggingInterceptor
    fun provideSharingLoggingInterceptor(@IsDebuggable isDebuggable: Boolean): HttpLoggingInterceptor? {
        return if (isDebuggable) {
            HttpLoggingInterceptor { message ->
                Timber.tag("ShareApi")
                Timber.d(message)
            }.also { it.level = HttpLoggingInterceptor.Level.BODY }
        }
        else null
    }

    @Provides
    @Singleton
    @SharingOkHttpClient
    fun provideSharingOkHttpClient(okHttpClient: OkHttpClient, @SharingLoggingInterceptor httpLoggingInterceptor: HttpLoggingInterceptor?): OkHttpClient {
        val builder = okHttpClient.newBuilder()

        httpLoggingInterceptor?.let {
            builder.addNetworkInterceptor(it)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideSharingService(@SharingOkHttpClient okHttpClient: OkHttpClient): SharingService {
        return Retrofit.Builder()
            .baseUrl("https://infomaker.io/")
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @SharingBaseUrl
    fun provideSharingBaseUrl(@GlobalLiveContentUiConfig liveContentUIConfig: LiveContentUIConfig): String? {
        return liveContentUIConfig.sharing?.shareApiUrl
    }

    @Provides
    @SharingAllowedDomains
    fun provideSharingAllowedDomains(@GlobalLiveContentUiConfig liveContentUIConfig: LiveContentUIConfig): List<String>? {
        return liveContentUIConfig.sharing?.allowedDomains
    }
}