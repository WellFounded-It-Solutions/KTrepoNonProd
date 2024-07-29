package se.infomaker.livecontentmanager.di

import com.navigaglobal.mobile.di.IsDebuggable
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.navigaglobal.mobile.auth.AuthorizationProvider
import com.navigaglobal.mobile.auth.AuthorizationProviderManager
import com.navigaglobal.mobile.auth.BasicAuthAuthorizationProvider
import com.navigaglobal.mobile.auth.ClientCredentialsAuthorizationProvider
import com.navigaglobal.mobile.auth.TokenService
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.network.AndroidNetworkAvailabilityManager
import se.infomaker.livecontentmanager.network.NetworkAvailabilityManager
import se.infomaker.livecontentmanager.query.QueryManager
import se.infomaker.livecontentmanager.query.lcc.LCCQueryManager
import se.infomaker.livecontentmanager.query.lcc.infocaster.InfocasterConnection
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerService
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerServiceBuilder
import se.infomaker.livecontentmanager.query.runnable.AndroidRunnableHandlerFactory
import se.infomaker.livecontentmanager.query.runnable.RunnableHandlerFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LccModule {

    @Binds
    abstract fun bindRunnableHandlerFactory(androidRunnableHandlerFactory: AndroidRunnableHandlerFactory): RunnableHandlerFactory

    @Binds
    abstract fun bindNetworkAvailabilityManager(androidNetworkAvailabilityManager: AndroidNetworkAvailabilityManager): NetworkAvailabilityManager

    @Binds
    abstract fun bindQueryManager(lccQueryManager: LCCQueryManager): QueryManager

    companion object {

        @Provides
        @QueryStreamerBaseUrl
        fun provideQueryStreamerBaseUrl(
            @GlobalLiveContentConfig liveContentConfig: LiveContentConfig, 
            @QueryStreamerV2BaseUrl liveContentV2BaseUrl: String?
        ): String {
            return liveContentV2BaseUrl ?: liveContentConfig.querystreamer ?: throw NullPointerException("No Query Streamer URL configured.")
        }

        @Provides
        @QueryStreamerV2BaseUrl
        fun provideQueryStreamerV2BaseUrl(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
            return liveContentConfig.querystreamerV2
        }

        @Provides
        @QueryStreamerBasicAuthUsername
        fun provideQueryStreamerUsername(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
            return liveContentConfig.querystreamerId
        }

        @Provides
        @QueryStreamerBasicAuthPassword
        fun provideQueryStreamerPassword(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
            return liveContentConfig.querystreamerReadToken
        }

        @Provides
        @QueryStreamerTokenServiceClientId
        fun provideQueryStreamerTokenServiceClientId(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
            return liveContentConfig.querystreamerClientId
        }

        @Provides
        @QueryStreamerTokenServiceClientSecret
        fun provideQueryStreamerTokenServiceClientSecret(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
            return liveContentConfig.querystreamerClientSecret
        }
        
        @Provides
        @QueryStreamerBasicAuthAuthorizationProvider
        fun provideBasicAuthorizationProvider(
            @QueryStreamerBasicAuthUsername username: String?,
            @QueryStreamerBasicAuthPassword password: String?
        ): AuthorizationProvider? {
            if (username == null || password == null) return null
            return BasicAuthAuthorizationProvider(username, password)
        }

        @Provides
        @QueryStreamerClientCredentialsAuthorizationProvider
        fun provideClientCredentialsAuthProvider(
            @QueryStreamerTokenServiceClientId clientId: String?,
            @QueryStreamerTokenServiceClientSecret clientSecret: String?,
            tokenService: TokenService?
        ): AuthorizationProvider? {
            if (clientId == null || clientSecret == null || tokenService == null) return null
            return ClientCredentialsAuthorizationProvider(clientId, clientSecret, tokenService)
        }

        @Provides
        @QueryStreamerAuthProvider
        fun provideQueryStreamerAuthProvider(
            @QueryStreamerV2BaseUrl queryStreamerV2BaseUrl: String?,
            @QueryStreamerClientCredentialsAuthorizationProvider clientCredentialsAuth: AuthorizationProvider?,
            @QueryStreamerBasicAuthAuthorizationProvider basicAuthAuthProvider: AuthorizationProvider?
        ): AuthorizationProvider {
            val preConfiguredAuthorizationProvider = if (!queryStreamerV2BaseUrl.isNullOrEmpty()) AuthorizationProviderManager.get(queryStreamerV2BaseUrl) else null
            return preConfiguredAuthorizationProvider
                ?: clientCredentialsAuth
                ?: basicAuthAuthProvider
                ?: throw NullPointerException("No valid QueryStreamer credentials configured.")
        }

        @Provides
        @QueryStreamerLoggingInterceptor
        fun provideQueryStreamerLoggingInterceptor(@IsDebuggable isDebuggable: Boolean): HttpLoggingInterceptor? {
            return if (isDebuggable) {
                HttpLoggingInterceptor { message ->
                    Timber.tag("QueryStreamer")
                    Timber.d(message)
                }.also { it.level = HttpLoggingInterceptor.Level.BODY }
            }
            else null
        }

        @Provides
        @Singleton
        @QueryStreamerOkHttpClient
        fun provideQueryStreamerOkHttpClient(okHttpClient: OkHttpClient, @QueryStreamerLoggingInterceptor interceptor: HttpLoggingInterceptor?): OkHttpClient {
            val builder = okHttpClient.newBuilder()

            interceptor?.let {
                builder.addNetworkInterceptor(it)
            }

            return builder.build()
        }

        @Provides
        @Singleton
        fun provideQueryStreamerService(
            @QueryStreamerBaseUrl queryStreamerBaseUrl: String,
            @QueryStreamerAuthProvider authorizationProvider: AuthorizationProvider,
            @QueryStreamerOkHttpClient okHttpClient: OkHttpClient
        ): QueryStreamerService {
            return QueryStreamerServiceBuilder()
                .setOkHttpClient(okHttpClient)
                .setBaseUrl(queryStreamerBaseUrl)
                .setAuthorizationProvider(authorizationProvider)
                .build()
        }

        @Provides
        @InfocasterUrl
        fun provideInfocasterUrl(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String {
            return liveContentConfig.infocaster ?: throw NullPointerException("No Infocaster URL configured.")
        }

        @Provides
        @InfocasterEventNotifierBroadcastId
        fun provideInfocasterEventNotifierBroadcastId(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String {
            return liveContentConfig.eventNotifierBroadcastId ?: throw NullPointerException("No eventNotifierBroadcastId configured.")
        }

        @Provides
        @InfocasterLoggingInterceptor
        fun provideInfocasterLoggingInterceptor(@IsDebuggable isDebuggable: Boolean): HttpLoggingInterceptor? {
            return if (isDebuggable) {
                HttpLoggingInterceptor { message ->
                    Timber.tag("Infocaster")
                    Timber.d(message)
                }.also { it.level = HttpLoggingInterceptor.Level.BODY }
            }
            else null
        }

        @Provides
        @Singleton
        @InfocasterOkHttpClient
        fun provideInfocasterOkHttpClient(okHttpClient: OkHttpClient, @InfocasterLoggingInterceptor interceptor: HttpLoggingInterceptor?): OkHttpClient {
            val builder = okHttpClient.newBuilder()

            interceptor?.let {
                builder.addNetworkInterceptor(it)
            }

            return builder.build()
        }

        @Provides
        @Singleton
        fun provideInfocasterConnection(
            @InfocasterUrl infocasterUrl: String,
            @InfocasterEventNotifierBroadcastId eventNotifierBroadcastId: String,
            @InfocasterOkHttpClient okHttpClient: OkHttpClient
        ): InfocasterConnection {
            return InfocasterConnection.Builder()
                .setUrl(infocasterUrl)
                .setEventNotifierBroadcastId(eventNotifierBroadcastId)
                .setOkHttpClient(okHttpClient)
                .create()
        }
    }
}